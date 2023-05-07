package top.kwseeker.mq.zerocopy;

import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * MappedByteBuffer 底层借助系统调用 `mmap64()` 实现，相比于 `mmap()` 可以映射更大的内存。
 */
public class MappedByteBufferTest {

    @Test
    public void testWrite() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("out/mbf.txt", "rw");
        //获取对应的通道
        FileChannel channel = randomAccessFile.getChannel();

        String content = "Hello world! Wrote by MappedByteBuffer";
        /**
         * mode：限定内存映射区域（MappedByteBuffer）对内存映像文件的访问模式，包括只可读（READ_ONLY）、可读可写（READ_WRITE）和写时拷贝（PRIVATE）三种模式。
         * position：文件映射的起始地址，对应内存映射区域（MappedByteBuffer）的首地址。
         * size：文件映射的字节长度，从 position 往后的字节数，对应内存映射区域（MappedByteBuffer）的大小。
         */
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, content.length());
        mappedByteBuffer.put(content.getBytes());

        mappedByteBuffer.force();
        randomAccessFile.close();
    }

    @Test
    public void testRead() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("out/mbf.txt", "rw");
        FileChannel channel = randomAccessFile.getChannel();

        long fileLen = randomAccessFile.length();
        System.out.println("file len: " + fileLen);
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileLen);
        byte[] bytes = new byte[(int) fileLen];
        mappedByteBuffer.get(bytes);
        System.out.println("file content: " + new String(bytes));

        randomAccessFile.close();
    }
}