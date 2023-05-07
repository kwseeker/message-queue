#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/sendfile.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>

#define BUF_SIZE 1024

// mmap + write 实现文件复制
void file_copy_mmw(const char *src_path, const char *dst_path) {
    int fd_src, fd_dst;
    struct stat st;

    // 打开源文件
    if ((fd_src = open(src_path, O_RDONLY)) == -1) {
        perror("open src file failed");
        return;
    }

    // 获取文件的大小
    if (fstat(fd_src, &st) == -1) {
        perror("fstat error");
        close(fd_src);
        return;
    }

    // 创建目标文件
    if ((fd_dst = open(dst_path, O_CREAT | O_WRONLY | O_TRUNC, 0644)) == -1) {
        perror("create dst file failed");
        close(fd_src);
        return;
    }

    struct timeval start_time, end_time;
    double cost_time = 0;
    gettimeofday(&start_time, NULL);

    // 将源文件映射到内存中
    // void * mmap(void *addr, size_t length, int prot, int flags, int fd, off_t offset);
    // addr：指定映射区域的开始地址，通常是 NULL，表示让操作系统自动分配一个合适的地方。
    // length：指定映射区域的长度，单位为字节。
    // prot：映射区域的保护方式，可以是以下几个值的按位或结果：
    //   PROT_EXEC：表示映射区域可执行。
    //   PROT_READ：表示映射区域可读。
    //   PROT_WRITE：表示映射区域可写。
    //   PROT_NONE：表示映射区域无法访问。
    // flags：指定映射区域的类型和映射方式，可以是以下几个值的按位或结果：
    //   MAP_PRIVATE：表示映射区域是私有的，即对该区域的写操作不会映射到原文件，而是会生成一个新的、与原文件无关的副本。
    //   MAP_SHARED：表示映射区域是共享的，即对该区域的写操作会映射到原文件。
    //   MAP_FIXED：表示映射区域的起始地址必须为指定的 addr 值，通常不需要使用此参数。
    //   MAP_ANONYMOUS：表示映射区域不与任何文件关联，通常用于分配一段内存并将其映射到进程的地址空间中。
    // fd：指定所要映射的文件描述符，通常是一个打开的文件描述符，如果使用 MAP_ANONYMOUS 参数，则需要传递为 -1。
    // offset：指定文件映射的起点，通常为 0，表示从文件的起始位置开始映射。
    // 注意：
    //   mmap 函数映射的区域必须是系统内存页大小的整数倍，通常为 4KB，且不能跨越多个文件，否则可能会发生异常。
    //   在使用 mmap 函数时需要注意保护映射区域的安全，避免出现悬空指针、越界访问等问题。
    //   同时，在使用完 mmap 映射的内存区域后需要调用 munmap 函数释放内存区域，以避免发生内存泄漏问题。
    // char *src = mmap(NULL, st.st_size, PROT_READ, MAP_PRIVATE, fd_src, 0);
    char *src = mmap64(NULL, st.st_size, PROT_READ, MAP_PRIVATE, fd_src, 0);
    if (src == MAP_FAILED) {
        perror("mmap error");
        close(fd_src);
        close(fd_dst);
        return;
    }

    // 将内存中的数据写入到目标文件中
    if (write(fd_dst, src, st.st_size) == -1) {
        perror("write error");
        munmap(src, st.st_size);
        close(fd_src);
        close(fd_dst);
        return;
    }

    gettimeofday(&end_time, NULL);
    cost_time = (end_time.tv_sec - start_time.tv_sec) + (double)(end_time.tv_usec - start_time.tv_usec)/1000000;
    printf("copy file (mmap + write) cost time: %lf\n", cost_time);

    // 释放文件映射到的内存区域
    munmap(src, st.st_size);
    close(fd_src);
    close(fd_dst);
    printf("Copy file finished!\n");
}

/*
    将文件通过普通拷贝的方式拷贝多个副本(read/write)
    gcc file_copy_mmw.c -o target/file_copy_mmw
    Usage: file_copy_mmw file1 file2

    $ /home/lee/mywork/java/message-queue/message-queue/zero-copy/linux/target/file_copy_mmw canal.deployer-1.1.6.tar.gz canal.deploy.tar.gz
    copy file (mmap + write) cost time: 0.070859
    copy file cost time: 0.074258

    这里测试拷贝 107.2MB 的压缩包，耗时 0.074258 s
*/
int main(int argc, char **argv) {
    if (argc != 3) {
        printf("Usage: %s <src_file> <dst_file>\n", argv[0]);
        return -1;
    }

    struct timeval start_time, end_time;
    gettimeofday(&start_time, NULL);

    file_copy_mmw(argv[1], argv[2]);

    gettimeofday(&end_time, NULL);
    double cost_time = (end_time.tv_sec - start_time.tv_sec) + (double)(end_time.tv_usec - start_time.tv_usec)/1000000;
    printf("copy file cost time: %lf\n", cost_time);

    return 0;
}