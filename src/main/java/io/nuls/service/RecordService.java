package io.nuls.service;

import io.nuls.Config;
import io.nuls.controller.vo.RecordData;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.log.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecordService {

    public static final String FILE_NAME = "address";

    public static final String SPLIT = ";";

    @Autowired
    Config config;

    public void saveRecord(RecordData recordData) {
        synchronized (this){
            try {
                List<RecordData> data = getAllRecords();
                if(hasRecord(data, recordData.getMd5())){
                    Log.error("md5 record exists");
                    return ;
                }
                data.add(recordData);
                saveRecordToFile(data);
            } catch (IOException e) {
                Log.error("save record file fail",e);
            }

        }
    }

    public Optional<RecordData> getRecord(String md5) throws IOException {
        return getAllRecords().stream().filter(d->d.getMd5().equals(md5)).findFirst();
    }

    public void removeRecord(String md5) {
        synchronized (this){
            try {
                List<RecordData> data = getAllRecords().stream().filter(d->!d.getMd5().equals(md5)).collect(Collectors.toList());
                saveRecordToFile(data);
            } catch (IOException e) {
                Log.error("save record file fail",e);
            }
        }
    }

    public boolean hasRecord(String md5) throws IOException {
        return hasRecord(getAllRecords(), md5);
    }

    public boolean hasRecord(List<RecordData> allRecords, String md5) throws IOException {
        return allRecords.stream().anyMatch(mad->mad.getMd5().equals(md5));
    }

    private List<RecordData> getAllRecords() throws IOException {
        List<RecordData> res = new ArrayList<>();
        File file = new File(getDataFile());
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        while (line != null) {
            String[] data = line.split(SPLIT);
            RecordData record = new RecordData();
            record.setAddress(data[0]);
            record.setMd5(data[1]);
            record.setName(data[2]);
            record.setRecordTime(data[3]);
            record.setRecordNumber(data[4]);
            record.setAddress(data[5]);
            res.add(record);
            line = reader.readLine();
        }
        reader.close();
        return res;
    }

    public List<RecordData> getRecordsByAddress(String address) throws IOException {
        return getAllRecords().stream().filter(i -> i.getAddress().equals(address)).collect(Collectors.toList());
    }

    private void saveRecordToFile(List<RecordData> data) throws IOException {
        File file = new File(getDataFile());
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (RecordData d : data) {
            String item = new StringBuilder()
                    .append(d.getAddress()).append(SPLIT)
                    .append(d.getMd5()).append(SPLIT)
                    .append(d.getName()).append(SPLIT)
                    .append(d.getRecordTime()).append(SPLIT)
                    .append(d.getRecordNumber()).append(SPLIT)
                    .append(d.getAuthor()).append(SPLIT).toString();
            writer.write(item);
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    private String getDataFile() {
        return config.getDataPath() + File.separator + FILE_NAME;
    }

}
