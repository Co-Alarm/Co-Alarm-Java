package com.example.mapsactivity;

class FStore {
    private String addr;
    private String code;
    private String name;
    private String remain_stat;

    String getAddr() {
        return addr;
    }
    void setAddr(String addr) {
        this.addr = addr;
    }

    String getCode() {
        return code;
    }
    void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    String getRemain_stat() {
        return remain_stat;
    }
    void setRemain_stat(String remain_stat) {
        this.remain_stat = remain_stat;
    }

}
