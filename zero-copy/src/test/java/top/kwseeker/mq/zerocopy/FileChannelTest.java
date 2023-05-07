package top.kwseeker.mq.zerocopy;

import org.junit.Test;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * FileChannel transferTo() 底层借助系统调用 `sendfile64()` 实现。
 */
public class FileChannelTest {

    @Test
    public void test() throws IOException {
        File srcFile = new File("out/mbf.txt");
        File dstFile = new File("out/mbf_copy.txt");

        FileInputStream srcFis = new FileInputStream(srcFile);
        FileChannel fisChannel = srcFis.getChannel();
        FileOutputStream dstFos = new FileOutputStream(dstFile);
        FileChannel fosChannel = dstFos.getChannel();

        fisChannel.transferTo(0, srcFile.length(), fosChannel);

        srcFis.close();
        dstFos.close();
    }
}
