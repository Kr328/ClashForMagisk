#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>

void print_help_exit();

int main(int argc, char **argv) {
    if ( argc < 2 )
        print_help_exit();
    
    if ( daemon(1, 0) ) {
        perror("daemon");
        return 1;
    }
    
    return execvp(argv[1], argv + 1);
}

void print_help_exit() {
    printf("Usage: daemonize <PROG> [ARGS...]\n");
    exit(1);
}
