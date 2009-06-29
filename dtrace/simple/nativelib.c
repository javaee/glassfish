#include <jni.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

JNIEXPORT jbyteArray JNICALL Java_ReadFile_loadFile
(JNIEnv * env, jobject jobj, jstring name) {
	caddr_t m;
	jbyteArray jb;
	jboolean iscopy;
	struct stat finfo;
	const char *mfile = (*env)->GetStringUTFChars(
	env, name, &iscopy);
	int fd = open(mfile, O_RDONLY);

	if (fd == -1) {
		printf("Could not open %s\n", mfile);
	}
	lstat(mfile, &finfo);
	m = mmap((caddr_t) 0, finfo.st_size,
	PROT_READ, MAP_PRIVATE, fd, 0);
	if (m == (caddr_t)-1) {
		printf("Could not mmap %s\n", mfile);
		return(0);
	}
	jb=(*env)->NewByteArray(env, finfo.st_size);
	(*env)->SetByteArrayRegion(env, jb, 0, 
	finfo.st_size, (jbyte *)m);
	close(fd);
	(*env)->ReleaseStringUTFChars(env, name, mfile);
	return (jb);
}

