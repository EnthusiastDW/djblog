package site.dengwei.utils;

import org.apache.avro.Schema;
import org.apache.avro.reflect.ReflectData;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;
import org.apache.parquet.io.SeekableInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * parquet格式文件的读写工具类
 *
 * @author DengWei
 * @since 2025-08-07
 */
public class ParquetUtil {

    // 不使用Hadoop的OutputFile实现
    private static class LocalOutputFile implements OutputFile {
        private final File file;

        public LocalOutputFile(File file) {
            this.file = file;
        }

        @Override
        public PositionOutputStream create(long blockSizeHint) throws IOException {
            // 确保父目录存在
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            return new PositionOutputStream() {
                private final FileOutputStream outputStream = new FileOutputStream(file);
                private long position = 0;

                @Override
                public long getPos() throws IOException {
                    return position;
                }

                @Override
                public void write(int b) throws IOException {
                    outputStream.write(b);
                    position++;
                }

                @Override
                public void write(byte[] b) throws IOException {
                    outputStream.write(b);
                    position += b.length;
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    outputStream.write(b, off, len);
                    position += len;
                }

                @Override
                public void flush() throws IOException {
                    outputStream.flush();
                }

                @Override
                public void close() throws IOException {
                    outputStream.close();
                }
            };
        }

        @Override
        public PositionOutputStream createOrOverwrite(long blockSizeHint) throws IOException {
            // 删除已存在的文件
            if (file.exists()) {
                file.delete();
            }
            return create(blockSizeHint);
        }

        @Override
        public boolean supportsBlockSize() {
            return false;
        }

        @Override
        public long defaultBlockSize() {
            return 0;
        }
    }

    // 不使用Hadoop的InputFile实现
    private static class LocalInputFile implements InputFile {
        private final File file;

        public LocalInputFile(File file) {
            this.file = file;
        }

        @Override
        public long getLength() throws IOException {
            return file.length();
        }

        @Override
        public SeekableInputStream newStream() throws IOException {
            return new SeekableInputStream() {
                private final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                private long position = 0;

                @Override
                public long getPos() throws IOException {
                    return position;
                }

                @Override
                public void seek(long newPos) throws IOException {
                    randomAccessFile.seek(newPos);
                    position = newPos;
                }

                @Override
                public void readFully(byte[] bytes) throws IOException {
                    randomAccessFile.readFully(bytes);
                    position += bytes.length;
                }

                @Override
                public void readFully(byte[] bytes, int start, int len) throws IOException {
                    randomAccessFile.readFully(bytes, start, len);
                    position += len;
                }

                @Override
                public int read(ByteBuffer buf) throws IOException {
                    byte[] temp = new byte[buf.remaining()];
                    int bytesRead = randomAccessFile.read(temp);
                    if (bytesRead != -1) {
                        buf.put(temp, 0, bytesRead);
                        position += bytesRead;
                    }
                    return bytesRead;
                }

                @Override
                public void readFully(ByteBuffer buf) throws IOException {
                    byte[] temp = new byte[buf.remaining()];
                    randomAccessFile.readFully(temp);
                    buf.put(temp);
                    position += temp.length;
                }

                @Override
                public int read() throws IOException {
                    int result = randomAccessFile.read();
                    if (result != -1) {
                        position++;
                    }
                    return result;
                }

                @Override
                public int read(byte[] b) throws IOException {
                    int bytesRead = randomAccessFile.read(b);
                    if (bytesRead != -1) {
                        position += bytesRead;
                    }
                    return bytesRead;
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    int bytesRead = randomAccessFile.read(b, off, len);
                    if (bytesRead != -1) {
                        position += bytesRead;
                    }
                    return bytesRead;
                }

                @Override
                public long skip(long n) throws IOException {
                    long skipped = randomAccessFile.skipBytes((int) n);
                    position += skipped;
                    return skipped;
                }

                @Override
                public int available() throws IOException {
                    return (int) (randomAccessFile.length() - position);
                }

                @Override
                public void close() throws IOException {
                    randomAccessFile.close();
                }
            };
        }
    }

    /**
     * 不使用Hadoop写入Parquet文件
     */
    public static <T> void writeWithoutHadoop(Collection<T> data, Class<T> type, String pathString) throws IOException {
        File file = new File(pathString);
        Schema schema = ReflectData.get().getSchema(type);

        LocalOutputFile outputFile = new LocalOutputFile(file);

        // 使用反射数据模型
        try (ParquetWriter<T> writer = AvroParquetWriter.<T>builder(outputFile)
                .withSchema(schema)
                .withDataModel(ReflectData.get())
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withWriterVersion(ParquetProperties.WriterVersion.PARQUET_1_0)
                .withPageSize(ParquetProperties.DEFAULT_PAGE_SIZE)
                .build()) {

            for (T item : data) {
                writer.write(item);
            }
        } catch (Exception e) {
            throw new IOException("Error writing parquet file", e);
        }
    }

    /**
     * 不使用Hadoop读取Parquet文件
     */
    public static <T> List<T> readWithoutHadoop(Class<T> type, String pathString) throws IOException {
        File file = new File(pathString);
        if (!file.exists()) {
            throw new IOException("File not found: " + pathString);
        }

        LocalInputFile inputFile = new LocalInputFile(file);

        List<T> result = new ArrayList<>();

        try (ParquetReader<T> reader = AvroParquetReader.<T>builder(inputFile)
                .withDataModel(ReflectData.get())
                .build()) {

            T record;
            while ((record = reader.read()) != null) {
                result.add(record);
            }
        } catch (Exception e) {
            throw new IOException("Error reading parquet file", e);
        }

        return result;
    }

    /**
     * 使用Hadoop写入Parquet文件（保留原有方法）
     */
    public static <T> void write(Collection<T> data, Class<T> type, String pathString) throws IOException {
        // 转调用不使用Hadoop的方法
        writeWithoutHadoop(data, type, pathString);
    }
}
