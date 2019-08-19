package io.nuls.service.dto;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

public class Record extends BaseNulsData {

    private byte[] address;

    public String md5;

    public String name;

    public String recordTime;

    public String recordNumber;

    public String author;


    public byte[]  getAddress() {
        return address;
    }

    public void setAddress(byte[]  address) {
        this.address = address;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(String recordTime) {
        this.recordTime = recordTime;
    }

    public String getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(String recordNumber) {
        this.recordNumber = recordNumber;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(address);
        stream.writeString(md5);
        stream.writeString(name);
        stream.writeString(recordTime);
        stream.writeString(recordNumber);
        stream.writeString(author);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        address = byteBuffer.readByLengthByte();
        md5 = byteBuffer.readString();
        name = byteBuffer.readString();
        recordTime = byteBuffer.readString();
        recordNumber = byteBuffer.readString();
        author = byteBuffer.readString();
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfBytes(address);
        s += SerializeUtils.sizeOfString(md5);
        s += SerializeUtils.sizeOfString(name);
        s += SerializeUtils.sizeOfString(recordTime);
        s += SerializeUtils.sizeOfString(recordNumber);
        s += SerializeUtils.sizeOfString(author);
        return s;
    }
}
