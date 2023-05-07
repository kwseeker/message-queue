#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>

#define BUF_SIZE 1024

// splice 实现文件复制
void file_copy_splice(const char *src_path, const char *dst_path) {
    int fd_in, fd_out;
    struct stat st;
    off_t off = 0; // 指定文件传输的起始位置
    ssize_t nread, nwrite;

    // 打开源文件
    if ((fd_in = open(src_path, O_RDONLY)) == -1) {
        perror("open src file failed");
        return;
    }

    // 创建目标文件
    if ((fd_out = open(dst_path, O_CREAT | O_WRONLY | O_TRUNC, 0644)) == -1) {
        perror("create dst file failed");
        close(fd_in);
        return;
    }

    int fd[2];  //fd[1]是管道写入端、fd[0]是输出端
    pid_t pid;
    // 创建管道，splice 需要通过管道中转传输数据
    if (pipe(fd) < 0) {
        perror("pipe");
        return;
    }

    struct timeval start_time, end_time;
    double cost_time = 0;
    gettimeofday(&start_time, NULL);

    do {
        // 从源文件读数据写入到管道写端
        // fd_in 和 fd_out 分别表示输入文件和输出文件的文件描述符，
        // off_in 和 off_out 则分别指定了传输的起始位置，通常设置为 NULL 表示从当前位置开始传输。
        // len 表示要传输的数据长度，
        // flags 表示传输方式，可以选择 SPLICE_F_MOVE 或 SPLICE_F_NONBLOCK，具体含义如下：
        //  SPLICE_F_MOVE：表示在传输过程中，输入文件描述符和输出文件描述符的数据会被删除。
        //  SPLICE_F_NONBLOCK：表示使用非阻塞方式进行传输。
        nread = splice(fd_in, NULL, fd[1], NULL, BUF_SIZE, SPLICE_F_MORE | SPLICE_F_MOVE);
        if (nread < 0) {
            perror("splice no more data");
            break;
        }
        // 从管道读端读数据写入目标文件
        nwrite = splice(fd[0], NULL, fd_out, NULL, nread, SPLICE_F_MORE | SPLICE_F_MOVE);
        if (nwrite != nread) {
            perror("write error");
        }
    } while (nread > 0);

    gettimeofday(&end_time, NULL);
    cost_time = (end_time.tv_sec - start_time.tv_sec) + (double)(end_time.tv_usec - start_time.tv_usec)/1000000;
    printf("copy file (splice) cost time: %lf\n", cost_time);

    close(fd_in);
    close(fd_out);
    printf("Copy file finished!\n");
}

/*
    将文件通过普通拷贝的方式拷贝多个副本(read/write)
    gcc -D_GNU_SOURCE file_copy_splice.c -o target/file_copy_splice
    Usage: file_copy_splice file1 file2

    $ /home/lee/mywork/java/message-queue/message-queue/zero-copy/linux/target/file_copy_splice canal.deployer-1.1.6.tar.gz canal.deploy.tar.gz
    copy file (splice) cost time: 0.228173
    copy file cost time: 0.228373

    这里测试拷贝 107.2MB 的压缩包，耗时 0.228373 s
*/
int main(int argc, char **argv) {
    if (argc != 3) {
        printf("Usage: %s <src_file> <dst_file>\n", argv[0]);
        return -1;
    }

    struct timeval start_time, end_time;
    gettimeofday(&start_time, NULL);

    file_copy_splice(argv[1], argv[2]);

    gettimeofday(&end_time, NULL);
    double cost_time = (end_time.tv_sec - start_time.tv_sec) + (double)(end_time.tv_usec - start_time.tv_usec)/1000000;
    printf("copy file cost time: %lf\n", cost_time);

    return 0;
}