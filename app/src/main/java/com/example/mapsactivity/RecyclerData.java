package com.example.mapsactivity;

import java.util.List;

class FStore {
    String addr;
    String code;
    String name;
    String remain_stat;

    public String getAddr() {
        return addr;
    }
    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getRemain_stat() {
        return remain_stat;
    }
    public void setRemain_stat(String remain_stat) {
        this.remain_stat = remain_stat;
    }

}
class FSList{
    List<FStore> fStoreList;
}
