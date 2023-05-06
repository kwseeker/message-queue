#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/sendfile.h>
#include <sys/stat.h>
#include <sys/types.h>

#define BUF_SIZE 1024

/*
    将文件通过普通拷贝的方式拷贝多个副本
*/
int main(int argc, char **argv) {
    int src_fd, dst_fd;
    char buf[BUF_SIZE];
    struct stat stat_buf;
    off_t offset = 0, file_size = 0;

    if (argc != 3) {
        printf("Usage: %s <src_file> <dst_file>\n", argv[0]);
        return -1;
    }

    src_fd = open(argv[1], O_RDONLY);
    if (src_fd == -1) {
        printf("Open source file failed.\n");
        return -1;
    }

    fstat(src_fd, &stat_buf);
    file_size = stat_buf.st_size;

    for (int i = 0;) {
    }

    //只写打开，如果文件不存在则创建
    dst_fd = open(argv[2], O_WRONLY|O_CREAT|O_TRUNC, stat_buf.st_mode);
    if (dst_fd == -1) {
        printf("Open destination file failed.\n");
        close(src_fd);
        return -1;
    }

    while (offset < file_size) {
        size_t len = BUF_SIZE;
        if ((offset + len) > file_size) {
            len = file_size - offset;
        }
        ssize_t ret = sendfile(dst_fd, src_fd, &offset, len);
        if (ret == -1) {
            printf("Write to destination file failed.\n");
            break;
        }
    }

    close(dst_fd);
    close(src_fd);
    return 0;
}