#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <grp.h>

void print_help_exit();

int main(int argc, char **argv) {
    if ( argc < 5 )
        print_help_exit();
    
    int failure = 0;

    gid_t groups[32];
    int groups_length = 0;

    char *p = strtok(argv[3], ",");
    while ( p != NULL ) {
        groups[groups_length++] = atoi(p);
        p = strtok(NULL, ",");
    }

    failure |= setgroups(groups_length, groups);
    failure |= setgid(atoi(argv[2]));
    failure |= setuid(atoi(argv[1]));
    
    if ( failure ) {
        perror("setuid|setgid");
        return 1;
    }
    
    execv(argv[4] ,argv + 4);

    perror("execv");

    return -1;
}

void print_help_exit() {
    printf("Usage: setuidgid <UID> <GID> <PROG> [ARGS...]\n");
    exit(1);
}
