#include<stdio.h>
#include<stdlib.h>

void changeName(char *);
int main()
{
char name = 0;
changeName(&name);
printf("%c", name);
return 0;
}
void changeName(char *name){
printf("1: %c\n", *name);
*name='a';
printf("2: %c\n",*name);
}
