#include <jni.h>
#include <string>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/inotify.h>
#include <sys/poll.h>
#include <linux/input.h>
#include <unistd.h>
#include "jsoncpp/json/json.h"
#include "DefData.h"
#include <list>
#include <sys/socket.h>
#include "getevent.h"

using namespace std;

extern "C" {

JavaVM *jvm;
#include "lstate.h"
#include "lauxlib.h"
#include "lualib.h"
#include "cutils/sockets.h"
void *runScript(void *path);

}

char *localSocketSend(int type, const char *content);


// 运行脚本路径
string luaPath;
//设置脚本停止
bool script_set_stop;

//
// 录制脚本保存路径
FILE *recordFile;
// 录制状态
bool isRecord = false;
bool isRun = false;

list<RecordEvent> keys;
int current_timestamp = 0;
bool isMove = false;
bool isDown = false;
int preX = -1, preY = -1;
int maxW = 0, maxH = 0, wmW = 0, wmH = 0;
char *devInputEvent;
// app 接受路径
char *clientPath;


class Bean {

private:
    int type;
    string msg;

public:

    Bean(int Type, string info) : type(Type), msg(info) {
        localSocketSend(Type, info.c_str());
    }

    int getType() { return type; }

    string getMsg() { return msg; }
};



void luaLineHook(lua_State *L, lua_Debug *ar) {
//    LOG("luaLineHook begin,ar->currentline =%d  ar->event = %d  script_set_stop = %d",
//        ar->currentline, ar->event, script_set_stop);
    if (ar->event == LUA_HOOKLINE || script_set_stop) {
        if (script_set_stop == true) {
            //调用用户退出回调
            lua_getglobal(L, "beforeUserExit");
            if (lua_pcall(L, 0, 0, 0) == 0) {
            }
            LOG("send User Exit single");
            luaL_error(L, "User Exit.");
        }
    }
}


void touch_Down(int id, int x, int y) {
    JNIEnv *jniEnv = NULL;
    jvm->AttachCurrentThread(&jniEnv, NULL);
    jvm->GetEnv((void **) &jniEnv, JNI_VERSION_1_4);

    if (jniEnv == NULL)
        return;
    //映射类
    jclass jtools = jniEnv->FindClass(_class_name);
    if (jtools == NULL)
        return;

    //映射静态方法
    //输入文字
    jmethodID callfunc = jniEnv->GetStaticMethodID(jtools, "touchDown", "(III)V");
    if (callfunc == NULL) {
        jniEnv->DeleteLocalRef(jtools);
        return;
    }
    //调用方法
    jniEnv->CallStaticVoidMethod(jtools, callfunc, id, x, y);
    jniEnv->DeleteLocalRef(jtools);
    jvm->DetachCurrentThread();
}


void touch_Move(int id, int x, int y) {

    JNIEnv *jniEnv = NULL;
    jvm->AttachCurrentThread(&jniEnv, NULL);
    jvm->GetEnv((void **) &jniEnv, JNI_VERSION_1_4);
    if (jniEnv == NULL)
        return;
    //映射类
    jclass jtools = jniEnv->FindClass(_class_name);
    if (jtools == NULL)
        return;

    //映射静态方法
    //输入文字
    jmethodID callfunc = jniEnv->GetStaticMethodID(jtools, "touchMove", "(III)V");
    if (callfunc == NULL) {
        jniEnv->DeleteLocalRef(jtools);
        return;
    }
    //调用方法
    jniEnv->CallStaticVoidMethod(jtools, callfunc, id, x, y);
    jniEnv->DeleteLocalRef(jtools);
    jvm->DetachCurrentThread();

}


void touch_Up(int id, int x, int y) {
    JNIEnv *jniEnv = NULL;
    jvm->AttachCurrentThread(&jniEnv, NULL);
    jvm->GetEnv((void **) &jniEnv, JNI_VERSION_1_4);

    if (jniEnv == NULL)
        return;
    //映射类
    jclass jtools = jniEnv->FindClass(_class_name);
    if (jtools == NULL)
        return;

    //映射静态方法
    //输入文字
    jmethodID callfunc = jniEnv->GetStaticMethodID(jtools, "touchUp", "(III)V");
    if (callfunc == NULL) {
        jniEnv->DeleteLocalRef(jtools);
        return;
    }
    //调用方法
    jniEnv->CallStaticVoidMethod(jtools, callfunc, id, x, y);
    jniEnv->DeleteLocalRef(jtools);
    jvm->DetachCurrentThread();
}


/*清除所有记录的点*/
void clearEvents() {
    JNIEnv *jniEnv = NULL;
    jvm->AttachCurrentThread(&jniEnv, NULL);
    jvm->GetEnv((void **) &jniEnv, JNI_VERSION_1_4);

    if (jniEnv == NULL)
        return;
    //映射类
    jclass jtools = jniEnv->FindClass(_class_name);
    if (jtools == NULL)
        return;

    //映射静态方法
    //输入文字
    jmethodID callfunc =
            jniEnv->GetStaticMethodID(jtools, "clearEvents", "()V");
    if (callfunc == NULL) {
        jniEnv->DeleteLocalRef(jtools);
        return;
    }
    //调用方法
    jniEnv->CallStaticVoidMethod(jtools, callfunc);
    jniEnv->DeleteLocalRef(jtools);
    jvm->DetachCurrentThread();
}


/*LocalSocketServer 服务端*/
void *localSocketServer(void *path) {
    char *dstPath = (char *) path;
    LOG(" localSocketServer %s", dstPath);
    int server_fd = socket_local_server("/mnt/sdcard/ttt",
                                        ANDROID_SOCKET_NAMESPACE_ABSTRACT, SOCK_STREAM);

    int s_fdListen = listen(server_fd, 4);
    while (true) {
        int socket = accept(server_fd, NULL, NULL);
        if (socket <= 0) continue;
        char recv_char[1024];
        memset(recv_char, 0, 1024);
        int rlen = read(socket, recv_char, 1024);
        LOG("rec = %s  %d ", recv_char, rlen);
        const char *ret = "{\"msg\":\"开始了吗哈哈哈哈\",\"type\":1024}";
        write(socket, ret, strlen(ret));
        LOG("ret  = %s  %d ", ret, strlen(ret));
        close(socket);
    }
}


/*LocalSocket 客户端*/
char *localSocketSend(int type, const char *content) {
    int socket = socket_local_client(clientPath, ANDROID_SOCKET_NAMESPACE_ABSTRACT, SOCK_STREAM);
    if (socket <= 0)
        return NULL;

    Json::FastWriter writer;
    Json::Value value;
    value["type"] = type;
    value["msg"] = content;
    value["status"] = (isRecord ? 1 : (isRun ? 2: 0)); // 1 录制  2 运行  0
    LOG("isRecord  = %d   isRun = %d", isRecord, isRun);
    const char *msg = writer.write(value).c_str();
    write(socket, msg, strlen(msg));
    shutdown(socket, SHUT_WR);
    char recv_char[1024];
    memset(recv_char, 0, 1024);
    int rlen = read(socket, recv_char, 1024);
    LOG(" localSocket rev = %s", recv_char);
    close(socket);
    return recv_char;
}


void writeRecord(RecordEvent it) {
    if (recordFile == NULL)
        return;
    LOG("writeRecord  %ld %d %d %d", it.type, it.value, it.code, it.timestamp);
    if (it.type == 0 && it.code == 0 && it.value == 0) {
        int x = -1, y = -1;
        //计算延时
        if (current_timestamp != 0) {
            long sleep_time = it.timestamp - current_timestamp;
            fprintf(recordFile, "mSleep(%d)\n", (int) sleep_time);
            //LOG("mSleep(%d)\n",(int)sleep_time);
        }
        for (RecordEvent item : keys) {
            //					System.out.println(item);
            if (item.type == 3 && item.code == 53 && item.type == 3) { // x
                preX = x = item.value;
            }
            if (item.type == 3 && item.code == 54 && item.type == 3) { // y
                preY = y = item.value;
            }
            // EV_KEY       BTN_TOUCH            DOWN
            if (item.type == 1 && item.code == 330 && item.value == 1) {// 写touchDown
                isMove = true;
                isDown = true;
                //EV_ABS       ABS_MT_TRACKING_ID   000000aa
            } else if (item.type == 3 && item.code == 57 && item.value > 0) { // 写touchDown
                isMove = true;
                isDown = true;
                //EV_KEY  BTN_TOUCH   UP    0001 014a 00000000
            } else if (item.type == 1 && item.code == 330 && item.value == 0) {// 写touchUP
                isMove = false;
                isDown = false;
                //EV_ABS       ABS_MT_TRACKING_ID   ffffffff
            } else if (item.type == 3 && item.code == 57 && item.value == -1) {// 写touchUp
                isMove = false;
                isDown = false;
            }
        }
        if (x == -1 && y != -1) {
            x = preX;
        } else if (x != -1 && y == -1) {
            y = preY;
        }
//        TSPoint point = {x, y};
//        TSPoint p = TSLuaOper::shard()->convertPointR(point, orient);
//        x = p.x;
//        y = p.y;
        if (isMove && isDown) { // down
            fprintf(recordFile, "touchDown(%d,%d)\n", x, y);
            isDown = false;
        } else if (isMove) {// 写入 touchDown
            if (x != -1 && y != -1) {
                fprintf(recordFile, "touchMove(%d,%d)\n", x, y);
            }
        } else if (isMove == false && isDown == false && preX != -1 && preY != -1) {
//            TSPoint point = {preX, preY};
//            TSPoint p = TSLuaOper::shard()->convertPointR(point, orient);
            fprintf(recordFile, "touchUp(%d,%d)\n", preX, preY);
        }
        current_timestamp = it.timestamp;
        keys.clear();
    } else {
        keys.push_back(it);
    }
}


static struct pollfd *ufds;
static char **device_names;
static char **device_disnames;
static int nfds;

enum {
    PRINT_DEVICE_ERRORS = 1U << 0,
    PRINT_DEVICE = 1U << 1,
    PRINT_DEVICE_NAME = 1U << 2,
    PRINT_DEVICE_INFO = 1U << 3,
    PRINT_VERSION = 1U << 4,
    PRINT_POSSIBLE_EVENTS = 1U << 5,
    PRINT_INPUT_PROPS = 1U << 6,
    PRINT_HID_DESCRIPTOR = 1U << 7,

    PRINT_ALL_INFO = (1U << 8) - 1,

    PRINT_LABELS = 1U << 16,
};


static const char *get_label(const struct label *labels, int value) {
    while (labels->name && value != labels->value) {
        labels++;
    }
    return labels->name;
}

static int print_possible_events(int fd) {
    uint8_t *bits = NULL;
    ssize_t bits_size = 0;
    const char *label;
    int i, j, k;
    int res, res2;

    LOG("  events:\n");
    for (i = EV_KEY; i <= EV_MAX; i++) {
        int count = 0;
        while (1) {
            res = ioctl(fd, EVIOCGBIT(i, bits_size), bits);
            if (res < bits_size)
                break;
            bits_size = res + 16;
            bits = (uint8_t *) realloc(bits, bits_size * 2);
            if (bits == NULL) {
                LOG("failed to allocate buffer of size %d\n",
                    bits_size);
                return 1;
            }
        }
        res2 = 0;
        switch (i) {
            case EV_SYN:
                label = "SYN";
                break;
            case EV_KEY:
                res2 = ioctl(fd, EVIOCGKEY(res), bits + bits_size);
                label = "KEY";
                break;
            case EV_REL:
                label = "REL";
                break;
            case EV_ABS:
                label = "ABS";
                break;
            case EV_MSC:
                label = "MSC";
                break;
            case EV_LED:
                res2 = ioctl(fd, EVIOCGLED(res), bits + bits_size);
                label = "LED";
                break;
            case EV_SND:
                res2 = ioctl(fd, EVIOCGSND(res), bits + bits_size);
                label = "SND";
                break;
            case EV_SW:
                res2 = ioctl(fd, EVIOCGSW(bits_size), bits + bits_size);
                label = "SW ";
                break;
            case EV_REP:
                label = "REP";
                break;
            case EV_FF:
                label = "FF ";
                break;
            case EV_PWR:
                label = "PWR";
                break;
            default:
                res2 = 0;
                label = "???";
        }
        int value = -1;
        for (j = 0; j < res; j++) {
            for (k = 0; k < 8; k++)
                if (bits[j] & 1 << k) {
                    char down;
                    if (j < res2 && (bits[j + bits_size] & 1 << k))
                        down = '*';
                    else
                        down = ' ';
                    if (count == 0)
                        LOG("    %s (%04x):", label, i);
                    else if ((count & 0x7) == 0 || i == EV_ABS)
                        LOG("\n               ");
                    LOG(" %04x%c", j * 8 + k, down);
                    value = j * 8 + k;
                    if (i == EV_ABS) {
                        struct input_absinfo abs;
                        if (ioctl(fd, EVIOCGABS(j * 8 + k), &abs) == 0) {
                            LOG(" value %d, min %d, max %d, fuzz %d flat %d",
                                abs.value, abs.minimum, abs.maximum,
                                abs.fuzz, abs.flat);

                            if (value == 0x0035) { // maxW
                                maxW = abs.maximum;
                            }
                            if (value == 0x0036) { // maxH
                                maxH = abs.maximum;
                            }
                        }
                    }
                    count++;
                }
        }
        if (count)
            LOG("\n");
    }
    free(bits);
    return 0;
}


int open_device(const char *device, int print_flags) {

    int version;
    int fd;
    struct pollfd *new_ufds;
    char **new_device_names;
    char **new_device_disnames;
    char name[80];
    char location[80];
    char idstr[80];
    struct input_id id;

    fd = open(device, O_RDWR);
    if (fd < 0) {
        if (print_flags & PRINT_DEVICE_ERRORS)
            LOG("could not open %s, %s\n", device, strerror(errno));
        return -1;
    }

    if (ioctl(fd, EVIOCGVERSION, &version)) {
        if (print_flags & PRINT_DEVICE_ERRORS)
            LOG("could not get driver version for %s, %s\n", device,
                strerror(errno));
        return -1;
    }
    if (ioctl(fd, EVIOCGID, &id)) {
        if (print_flags & PRINT_DEVICE_ERRORS)
            LOG("could not get driver id for %s, %s\n", device,
                strerror(errno));
        return -1;
    }
    name[sizeof(name) - 1] = '\0';
    location[sizeof(location) - 1] = '\0';
    idstr[sizeof(idstr) - 1] = '\0';
    if (ioctl(fd, EVIOCGNAME(sizeof(name) - 1), &name) < 1) {
        LOG("could not get device name for %s, %s\n", device, strerror(errno));
        name[0] = '\0';
    }
    if (ioctl(fd, EVIOCGPHYS(sizeof(location) - 1), &location) < 1) {
        LOG("could not get location for %s, %s\n", device, strerror(errno));
        location[0] = '\0';
    }
    if (ioctl(fd, EVIOCGUNIQ(sizeof(idstr) - 1), &idstr) < 1) {
        //ERROR( "could not get idstring for %s, %s\n", device, strerror(errno));
        idstr[0] = '\0';
    }

    new_ufds = (pollfd *) realloc(ufds, sizeof(ufds[0]) * (nfds + 1));
    if (new_ufds == NULL) {
        LOG("out of memory\n");
        return -1;
    }
    ufds = new_ufds;
    new_device_names = (char **) realloc(device_names,
                                         sizeof(device_names[0]) * (nfds + 1));

    new_device_disnames = (char **) realloc(device_disnames,
                                            sizeof(device_disnames[0]) * (nfds + 1));

    if (new_device_names == NULL) {
        LOG("out of memory\n");
        return -1;
    }

    if (new_device_disnames == NULL) {
        LOG("out of memory\n");
        return -1;
    }

    device_names = new_device_names;
    device_disnames = new_device_disnames;

    if (print_flags & PRINT_DEVICE)
        LOG("add device %d: %s\n", nfds, device);
    if (print_flags & PRINT_DEVICE_INFO)
        LOG("  bus:      %04x\n"
                    "  vendor    %04x\n"
                    "  product   %04x\n"
                    "  version   %04x\n", id.bustype, id.vendor, id.product,
            id.version);
    if (print_flags & PRINT_DEVICE_NAME)
        LOG("  name:     \"%s\"\n", name);
    if (print_flags & PRINT_DEVICE_INFO)
        LOG("  location: \"%s\"\n"
                    "  id:       \"%s\"\n", location, idstr);
    if (print_flags & PRINT_VERSION)
        LOG("  version:  %d.%d.%d\n", version >> 16, (version >> 8) & 0xff,
            version & 0xff);

    if (print_flags & PRINT_POSSIBLE_EVENTS && maxW == 0 && maxH == 0) {
        print_possible_events(fd);
        LOG("w = %d h = %d", maxW, maxH);
        if (maxW && maxH && devInputEvent == NULL) {
            devInputEvent = new char[strlen(device)];
            memset(devInputEvent, 0, strlen(device));
            strcpy(devInputEvent, device);

        }
    }

    ufds[nfds].fd = fd;
    ufds[nfds].events = POLLIN;
    device_names[nfds] = strdup(device);
    device_disnames[nfds] = strdup(name);
    nfds++;

    return 0;
}

int close_device(const char *device, int print_flags) {
    int i;
    for (i = 1; i < nfds; i++) {
        if (strcmp(device_names[i], device) == 0) {
            int count = nfds - i - 1;
            if (print_flags & PRINT_DEVICE)
                LOG("remove device %d: %s\n", i, device);
            free(device_names[i]);
            memmove(device_names + i, device_names + i + 1,
                    sizeof(device_names[0]) * count);
            memmove(ufds + i, ufds + i + 1, sizeof(ufds[0]) * count);
            nfds--;
            return 0;
        }
    }
    if (print_flags & PRINT_DEVICE_ERRORS)
        LOG("remote device: %s not found\n", device);
    return -1;
}

static int read_notify(const char *dirname, int nfd, int print_flags) {
    int res;
    char devname[PATH_MAX];
    char *filename;
    char event_buf[512];
    int event_size;
    int event_pos = 0;
    struct inotify_event *event;

    res = read(nfd, event_buf, sizeof(event_buf));
    if (res < (int) sizeof(*event)) {
        if (errno == EINTR)
            return 0;
        LOG("could not get event, %s\n", strerror(errno));
        return 1;
    }
    //printf("got %d bytes of event information\n", res);

    strcpy(devname, dirname);
    filename = devname + strlen(devname);
    *filename++ = '/';

    while (res >= (int) sizeof(*event)) {
        event = (struct inotify_event *) (event_buf + event_pos);
        //printf("%d: %08x \"%s\"\n", event->wd, event->mask, event->len ? event->name : "");
        if (event->len) {
            strcpy(filename, event->name);
            if (event->mask & IN_CREATE) {
                open_device(devname, print_flags);
            } else {
                close_device(devname, print_flags);
            }
        }
        event_size = sizeof(*event) + event->len;
        res -= event_size;
        event_pos += event_size;
    }
    return 0;
}

static int scan_dir(const char *dirname, int print_flags) {
    char devname[PATH_MAX];
    char *filename;
    DIR *dir;
    struct dirent *de;
    dir = opendir(dirname);
    if (dir == NULL)
        return -1;
    strcpy(devname, dirname);
    filename = devname + strlen(devname);
    *filename++ = '/';
    while ((de = readdir(dir))) {
        if (de->d_name[0] == '.'
            && (de->d_name[1] == '\0'
                || (de->d_name[1] == '.' && de->d_name[2] == '\0')))
            continue;
        strcpy(filename, de->d_name);
        open_device(devname, print_flags);
    }
    closedir(dir);
    return 0;
}


void *record(void *args) {
    LOG("Java_www_lsh_com_kotlin_MainActivity_stringFromJNI   record");
    int i;
    int res;
    char *newline = "\n";
    struct input_event event;
    int print_flags = PRINT_DEVICE_ERRORS | PRINT_DEVICE | PRINT_DEVICE_NAME;
    int print_flags_set = 0;
    int event_count = 0;
    int sync_rate = 0;
    int64_t last_sync_time = 0;
    const char *device = NULL;
    const char *device_path = "/dev/input";
    LOG("getevent_main begin");
    //打开设备的读权限
    FILE *pp = popen("su root", "w"); //建立管道
    if (!pp) {
        return NULL;
    }
    fputs("chmod 666 /dev/input/*\n", pp);
    pclose(pp); //关闭管道
    print_flags = PRINT_DEVICE_ERRORS | PRINT_DEVICE | PRINT_DEVICE_NAME
                  | PRINT_POSSIBLE_EVENTS;
    print_flags_set = 1;

    nfds = 1;
    ufds = (pollfd *) calloc(1, sizeof(ufds[0]));
    ufds[0].fd = inotify_init();
    ufds[0].events = POLLIN;
    if (device) {
        if (!print_flags_set)
            print_flags = PRINT_DEVICE_ERRORS;
        res = open_device(device, print_flags);
        if (res < 0) {
            return NULL;
        }
    } else {
        res = inotify_add_watch(ufds[0].fd, device_path, IN_DELETE | IN_CREATE);
        if (res < 0) {
            LOG("could not add watch for %s, %s\n", device_path,
                strerror(errno));
            return NULL;
        }
        res = scan_dir(device_path, print_flags);
        if (res < 0) {
            LOG("scan dir failed for %s\n", device_path);
            return NULL;
        }
    }

    LOG("begin while %d   dev %s", isRecord, devInputEvent);

    struct timeval tv;
    gettimeofday(&tv, NULL);
    //初始化时间戳
    current_timestamp = (tv.tv_sec % (12 * 3600) * 1000 + tv.tv_usec / 1000);
    //清空记录
    keys.clear();

//    float scaleW = wmH/maxH, scaleH = 1;

    while (true) {
        poll(ufds, nfds, -1);
        if (ufds[0].revents & POLLIN) {
            read_notify(device_path, ufds[0].fd, print_flags);
        }
        for (i = 1; i < nfds; i++) {
            if (ufds[i].revents) {
                if (ufds[i].revents & POLLIN) {
                    res = read(ufds[i].fd, &event, sizeof(event));
                    if (res < (int) sizeof(event)) {
                        LOG("could not get event\n");
                        return NULL;
                    }

//                    LOG("%s %s: %04x %04x %08x", device_names[i], device_disnames[i], event.type,
//                        event.code, event.value);
                    // 录制模块
                    if (strstr(device_names[i], devInputEvent) && isRecord) {
                        LOG("%04d %04d %04d", event.type, event.code, event.value);
                        bool record_it = false;
                        if (event.type == 3 && event.code == 47) {
                            record_it = true;
                        } else if (event.type == 3 && event.code == 57) {
                            record_it = true;
                        } else if (event.type == 3 && event.code == 53) {  // x
                            record_it = true;
                        } else if (event.type == 3 && event.code == 54) { // y
                            record_it = true;
                        } else if (event.type == 0 && event.code == 0 && event.value == 0) {
                            record_it = true;
                        } else if (event.type == 1 && event.code == 330 &&
                                   (event.value == 1 || event.value == 0)) {// DOWN 和 UP
                            record_it = true;
                        }
                        if (record_it) {
                            //tv.tv_sec 秒  , tv.tv_usec 纳秒
                            gettimeofday(&tv, NULL);
                            RecordEvent point = {event.type, event.code,
                                                 event.code == 53 ? event.value * wmW / maxW : (
                                                         event.code == 54 ? event.value * wmH / maxH
                                                                          : event.value),
                                                 tv.tv_sec % (12 * 3600) * 1000 +
                                                 tv.tv_usec / 1000};
                            writeRecord(point);
                        }
                    } else {
                        if (event.type == 1 && event.code == 115 && event.value == 0) {
                            localSocketSend(KEY_VOLUMEUP, "");

                        } else if (event.type == 1 && event.code == 114 && event.value == 0) {
//                            JNIEnv *env;
//                            jvm->AttachCurrentThread(&env, NULL);
//                            //音量下抬起
//                            LOG("音量下抬起  音量下抬起 ");
//                            jvm->DetachCurrentThread();
                              localSocketSend(KEY_VOLUMEDOWN, "");
                        }
                    }
                    if (event_count && --event_count == 0)
                        return 0;
                }
            }
        }
    }
    LOG("结束录制");
    pthread_detach(pthread_self());
    pthread_exit(NULL);
    return NULL;
}


/*设置脚本路径*/
Bean *setPath(string path) {
    if (path.size() == 0) new Bean(ERROR_CODE, PATH_NOT_EMPTY);
    luaPath = path;
    return new Bean(SET_PATH, SET_PATH_STR);
}

/*运行脚本*/
Bean *runLua() {
    if (isRun) {
        return new Bean(ERROR_CODE, LUN_IS_RUN);
    }
    if (isRecord) {
        return new Bean(ERROR_CODE, RECORD);
    }
    // 先检测是否设置路径
    if (luaPath.size() == 0) {
        return new Bean(ERROR_CODE, NOT_SET_LUA_PATH);
    }
    // 运行脚本
//    runScript(luaPath);
    script_set_stop = false;
    pthread_t pthread;
    char *buf = new char[luaPath.size() + 1];
    memset(buf, 0, sizeof(buf));
    strcpy(buf, luaPath.c_str());
    //创建线程
    int ret = pthread_create(&pthread, NULL, runScript, buf);
    LOG("runLua");
    return new Bean(RUN_LUA, RUN_LUA_STR);
}

/*停止脚本*/
Bean *stopLua() {
    // 先检测是否设置路径
    isRun = false;
    script_set_stop = true;
    return new Bean(STOP_LUA, STOP_LUA_STR);
}

/*开始录制脚本*/
Bean *recordLua(string path) {
    if (isRecord) {
        return new Bean(ERROR_CODE, RECORD);
    }
    if (isRun) {
        return new Bean(ERROR_CODE, LUN_IS_RUN);
    }
    if (path.size() == 0) return new Bean(ERROR_CODE, NOT_SET_RECORD_LUA_PATH);

    struct timeval tv;
    gettimeofday(&tv, NULL);
    //初始化时间戳
    current_timestamp = (tv.tv_sec % (12 * 3600) * 1000 + tv.tv_usec / 1000);
    recordFile = fopen(path.c_str(), "w");
    isRecord = true;
    return new Bean(START_RECORD, START_RECORD_STR);
}

/*停止录制脚本*/

Bean *stopRecordLua() {
    isRecord = false;
    if (recordFile) {
        fclose(recordFile);
    }
    return new Bean(START_RECORD, STOP_RECORD_STR);
}

/*解析 请求 */
Bean *dealRequest(char *request) {
    Json::Reader reader;
    Json::Value json_root;
    if (!reader.parse(string(request), json_root, false)) {
        LOG("auth 无效的json");
        return new Bean(-1, "无效的json");
    }
    if (json_root["type"].isInt() && json_root["msg"].isString()) {
        int type = json_root["type"].asInt();
        string msg = json_root["msg"].asString();
        switch (type) {
            case SET_PATH :
                return setPath(msg);
            case RUN_LUA:
                return runLua();
            case STOP_LUA:
                return stopLua();
            case START_RECORD:
                return recordLua(msg);
            case STOP_RECORD:
                return stopRecordLua();
            case ERROR_CODE:
                return new Bean(ERROR_CODE, "服务启动成功");
            default:
                return new Bean(ERROR_CODE, "json内容不对");
        }
    } else {
        return new Bean(-1, "无效的json");
    }
}


const char *bean2Json(Bean *bean) {
    Json::FastWriter writer;
    Json::Value value;
    value["type"] = bean->getType();
    value["msg"] = bean->getMsg();
    return writer.write(value).c_str();
}

/*处理socket 请求*/
void *dealSocket(void *param) {
    int socket = 0;
    memcpy(&socket, param, 4);
    LOG("接收到网络请求(%d)\n", socket);
    char recv_char[1024];
    memset(recv_char, 0, 1024);
    int rlen = read(socket, recv_char, 1024);
    LOG("rec = %s  %d ", recv_char, rlen);
    Bean *bean = dealRequest(recv_char);
    const char *ret = bean2Json(bean);
    write(socket, ret, strlen(ret));
    LOG("ret  = %s  ", ret, strlen(ret));
    close(socket);
    pthread_detach(pthread_self());
    pthread_exit(NULL);
    return NULL;

}


int m_msleep_wait_pdes[2];


void sleepWait(int msec) {
//	LOG("pre %d %d", m_msleep_wait_pdes[0], m_msleep_wait_pdes[1]);
    pipe(m_msleep_wait_pdes);
//	LOG("end %d %d", m_msleep_wait_pdes[0], m_msleep_wait_pdes[1]);
    //等待时间
    struct timeval timeout;
    timeout.tv_sec = msec / 1000;
    timeout.tv_usec = msec % 1000 * 1000; //单位微秒

    fd_set readfds;
    FD_ZERO(&readfds);
    FD_SET(m_msleep_wait_pdes[0], &readfds);

    int maxfd = 0;
    if (m_msleep_wait_pdes[0] > maxfd)
        maxfd = m_msleep_wait_pdes[0];

    int res = select(maxfd + 1, &readfds, NULL, NULL, &timeout);
    close(m_msleep_wait_pdes[0]);
    close(m_msleep_wait_pdes[1]);
}


void sleepOver() {
    write(m_msleep_wait_pdes[1], "exit", 4);
}


void clickPoint(lua_State *L, void (*ptr)(int, int, int)) {
    int idx = 0;
    int x = 0, y = 0;
    if (lua_gettop(L) == 0) {
        //没有写参数
        x = 0;
        y = 0;
    } else if (lua_gettop(L) == 2) {
        x = luaL_checknumber(L, 1);
        y = luaL_checknumber(L, 2);
    } else if (lua_gettop(L) == 3) {
        idx = luaL_checkinteger(L, 1);
        x = luaL_checknumber(L, 2);
        y = luaL_checknumber(L, 3);
    }
    (*ptr)(idx, x, y);
}


/*获取屏幕宽高*/
void getWMSize() {
//    wm size
//    Physical size: 768x1280
    FILE *p = popen("wm size", "r");
    if (p) {
        char line[100];
        fgets(line, 100, p);
        LOG("getWMSize  shell > %s", line);
        wmW = atoi(strstr(line, ":") + 1);
        wmH = atoi(strstr(line, "x") + 1);
        pclose(p);
    }
}

extern "C" {

static lua_State *_luaState = NULL;

int test(lua_State *L) {
    LOG("lua  _  test");
    return 0;
}
int touchDown(lua_State *L) {
    clickPoint(L, touch_Down);
    return 0;
}
int touchMove(lua_State *L) {
    clickPoint(L, touch_Move);
    return 0;
}
int touchUp(lua_State *L) {
    clickPoint(L, touch_Up);
    return 0;
}


int luaSleep(lua_State *L) {
    unsigned int time = (unsigned int) luaL_checkint(L, 1);
    sleepWait(time);
    return 0;
}


/*运行脚本*/
void *runScript(void *path) {
    _luaState = luaL_newstate();
    // 注册函数
    lua_register(_luaState, "test", test);
    lua_register(_luaState, "touchMove", touchMove);
    lua_register(_luaState, "touchDown", touchDown);
    lua_register(_luaState, "touchUp", touchUp);
    lua_register(_luaState, "mSleep", luaSleep);
    lua_sethook(_luaState, &luaLineHook,
                LUA_MASKCALL | LUA_MASKLINE | LUA_MASKRET | LUA_MASKCOUNT, 1);

    luaL_openlibs(_luaState);
    luaopen_base(_luaState);
    luaopen_table(_luaState);
    luaopen_string(_luaState);
    luaopen_math(_luaState);
    lua_settop(_luaState, 0);
    LOG("lua_run_start %s ", (char *) path);
    localSocketSend(RUN_LUA, "开始运行");
    isRun = true;
    int ret = luaL_dofile(_luaState, (char *) path);
    if (ret != 0) {
        const char *msg =  lua_tostring(_luaState, -1);
        LOG("error_str=%s", msg);
        localSocketSend(ERROR_CODE, (char *)msg);
    }
//    delete[] (char[])path;
    clearEvents();
    sleepOver();
    isRun = false;
    LOG("lua _run _end");
    // 脚本停止了
    localSocketSend(STOP_LUA, STOP_LUA_STR);
    pthread_detach(pthread_self());
    pthread_exit(NULL);

}


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOG("JNI_OnLoad");
    //保存java虚拟机
    jvm = vm;
    getWMSize();
    return JNI_VERSION_1_6;
}


JNIEXPORT void JNICALL Java_com_cashmaker_android_Shell_localServer
        (JNIEnv *env, jclass, jstring path) {
    // 开启线程录制脚本
    pthread_t bc_thread_id;
    //创建线程
    int ret = pthread_create(&bc_thread_id, NULL, record, NULL);
    LOG(" ret = %d", ret);
    // 开启监听服务
    const char *cpath = env->GetStringUTFChars(path, 0);
    char pkg[strlen(cpath)];
    memset(pkg, 0, strlen(cpath));
    strcpy(pkg, cpath);
    env->ReleaseStringUTFChars(path, cpath);
    LOG("开始localServer %s", pkg);
    // 开始localServer
    int server_fd = socket_local_server(pkg, ANDROID_SOCKET_NAMESPACE_ABSTRACT, SOCK_STREAM);
    int s_fdListen = listen(server_fd, 4);
    while (true) {
        int socket = accept(server_fd, NULL, NULL);
        if (socket <= 0) continue;
        //服务线程
        pthread_t connect_thread_id;
        char data[4];
        memcpy(data, &socket, 4);
        //创建线程
        int ret = pthread_create(&connect_thread_id, NULL, dealSocket, (void *) data);
        if (ret != 0) {
            LOG("create connection pthread error!\n");
        }
    }
}


JNIEXPORT void JNICALL Java_com_cashmaker_android_Shell_setLocalServerPath
        (JNIEnv *env, jclass, jstring path) {
    const char *cpath = env->GetStringUTFChars(path, 0);
    clientPath = (char *) malloc(strlen(cpath));
    memset(clientPath, 0, strlen(cpath));
    strcpy(clientPath, cpath);
    env->ReleaseStringUTFChars(path, cpath);
    LOG("setLocalServerPath %s", clientPath);
}



JNIEXPORT void JNICALL Java_com_cashmaker_android_act_InjectActivity_changeMethod
        (JNIEnv *env, jobject, jobject src, jobject dst) {

    LOG(" Java_com_cashmaker_android_act_InjectActivity_changeMethod ");
//   jmethodID  src1 = env->FromReflectedMethod(src);
//   jmethodID  src2 = env->FromReflectedMethod(dst);
//    memcpy(src2, src1,  sizeof(src1));
    LOG(" Java_com_cashmaker_android_act_InjectActivity_changeMethod  end");

}


}