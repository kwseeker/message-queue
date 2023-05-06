#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/sendfile.h>
#include <sys/stat.h>
#include <sys/types.h>

#define BUF_SIZE 1024

/*
    将文件通过普通拷贝的方式拷贝多个副本(read/write)
*/
int main(int argc, char **argv) {
    int src_fd, dst_fd;
    char buf[BUF_SIZE];
    ssize_t ret;

    if (argc != 3) {
        printf("Usage: %s <src_file> <dst_file>\n", argv[0]);
        return -1;
    }

    src_fd = open(argv[1], O_RDONLY);
    if (src_fd == -1) {
        printf("Open source file failed.\n");
        return -1;
    }

    dst_fd = open(argv[2], O_WRONLY|O_CREAT|O_TRUNC, 0666);
    if (dst_fd == -1) {
        printf("Open destination file failed.\n");
        close(src_fd);
        return -1;
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
    return 0;
}