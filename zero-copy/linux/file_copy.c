#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/sendfile.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>

#define BUF_SIZE 1024

void file_copy(const char *src_path, const char *dst_path) {
    char buf[BUF_SIZE];
    ssize_t ret;
    int src_fd, dst_fd;

    src_fd = open(src_path, O_RDONLY);
    if (src_fd == -1) {
        printf("Open source file failed.\n");
        return;
    }

    //只写模式打开文件，文件不存在就创建，如果文件存在将内容清空 O_TRUNC (truncate)
    dst_fd = open(dst_path, O_WRONLY|O_CREAT|O_TRUNC, 0666);
    if (dst_fd == -1) {
        printf("Open destination file failed.\n");
        close(src_fd);
        return;
    }

    while (1) {
        ret = read(src_fd, buf, BUF_SIZE);
        if (ret == -1) {
            printf("Read source file failed.\n");
            break;
        } else if (ret == 0) {
            break;
        }

        if (write(dst_fd, buf, ret) != ret) {
            printf("Write to destination file failed.\n");
            break;
        }
    }

    close(dst_fd);
    close(src_fd);
    printf("Copy file finished!\n");
}

/*
    将文件通过普通拷贝的方式拷贝多个副本(read/write)
    gcc file_copy.c -o target/file_copy
    Usage: file_copy file1 file2

    $ /home/lee/mywork/java/message-queue/message-queue/zero-copy/linux/target/file_copy canal.deployer-1.1.6.tar.gz canal.deploy.tar.gz
    Copy file cost time 0.216440
    这里测试拷贝 107.2MB 的压缩包，耗时 0.216440 s
*/
int main(int argc, char **argv) {
    if (argc != 3) {
        printf("usage: %s <src_file> <dst_file>\n", argv[0]);
        return -1;
    }

    struct timeval start_time, end_time;
    double cost_time = 0;
    gettimeofday(&start_time, NULL);

    file_copy(argv[1], argv[2]);

    gettimeofday(&end_time, NULL);
    cost_time = (end_time.tv_sec - start_time.tv_sec) + (double)(end_time.tv_usec - start_time.tv_usec)/1000000;
    printf("copy file cost time: %lf\n", cost_time);

    return 0;
}