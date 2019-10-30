#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <grp.h>
#include <string.h>
#include <stdbool.h>

#define MAX_GROUP_COUNT 32
#define BUFFER_SIZE 1024
#define MIN_ARGUMENT_COUNT 5
#define INTEGER_BASE 10

void print_help_exit();

int main(int argc, char **argv) {
    if ( argc < MIN_ARGUMENT_COUNT )
        print_help_exit();

    bool failure = false;

    gid_t groups[MAX_GROUP_COUNT];
    size_t groups_length = 0;
    char buffer[BUFFER_SIZE];

    strncpy(buffer, argv[3], BUFFER_SIZE);

    char *p = strtok(buffer, ",");
    while ( p != NULL && groups_length < MAX_GROUP_COUNT ) {
        groups[groups_length++] = (gid_t) strtoul(p, NULL, INTEGER_BASE);
        p = strtok(NULL, ",");
    }

    failure = failure || setgroups(groups_length, groups);
    failure = failure || setgid((gid_t) strtoul(argv[2], NULL, INTEGER_BASE));
    failure = failure || setuid((gid_t) strtoul(argv[1], NULL, INTEGER_BASE));
    
    if ( failure ) {
        perror("setuid|setgid|setgroups");
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
