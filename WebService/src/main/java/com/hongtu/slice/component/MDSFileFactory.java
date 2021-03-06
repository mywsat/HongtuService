package com.hongtu.slice.component;

import com.google.common.base.Throwables;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.hongtu.slice.config.Configuration;
import com.hongtu.slice.util.MDSInfo;
import com.hongtu.slice.util.SliceParameter;
import com.hongtu.slice.util.MDSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class MDSFileFactory {
    private Logger LOGGER = LoggerFactory.getLogger(MDSFileFactory.class);

    private Configuration configuration;
    private GuanvaCacheGetter mdsFileCache;
    private GuanvaCacheGetter sliceCache;


    public MDSFile generateMDSFile(String fileName) {
        MDSFile mdsFile = null;
        if (mdsFileCache != null)
            mdsFile = (MDSFile) mdsFileCache.get(fileName);
        if (mdsFile == null) {
            mdsFile = new MDSFile(configuration.getMDSFileProperties());
            mdsFile.init(fileName);
            mdsFileCache.set(fileName, mdsFile);
        }
        return mdsFile;
    }

    public byte[] getTileData(SliceParameter sliceParameter) {
        byte[] data = null;
        HashCode code = Hashing.md5().hashString(sliceParameter.toString(), Charset.defaultCharset());
        if (sliceCache != null) {
            data = (byte[]) sliceCache.get(code);
            if (data == null) {
                MDSFile mdsFile = generateMDSFile(sliceParameter.getPath());
                if (mdsFile != null) {
                    data = mdsFile.getTileData(sliceParameter.getLevel(), sliceParameter.getX(), sliceParameter.getY());
                    if (data != null) {
                        sliceCache.set(code, data);
                    }
                }
            }
        }
        return data;
    }

    public MDSInfo getMDSInfo(SliceParameter sliceParameter) {
        MDSInfo mdsInfo = null;
        HashCode code = Hashing.md5().hashString(sliceParameter.toString(), Charset.defaultCharset());
        if (mdsFileCache != null) {
            mdsInfo = (MDSInfo) mdsFileCache.get(code);
            if (mdsInfo == null) {
                MDSFile mdsFile = generateMDSFile(sliceParameter.getPath());
                if (mdsFile != null) {
                    mdsInfo = new MDSInfo();
                    mdsInfo.setCellHeight(mdsFile.getCellHeight());
                    mdsInfo.setCellWidth(mdsFile.getCellWidth());
                    mdsInfo.setHeight(mdsFile.getHeight());
                    mdsInfo.setWidth(mdsFile.getWidth());
                    mdsInfo.setLayerCount(mdsFile.getLayerCount());
                    mdsInfo.setMaxLevel(mdsFile.getMaxLevel());
                    mdsInfo.setMinLevel(mdsFile.getMinLevel());
                    mdsFileCache.set(code, mdsInfo);
                }
            }
        }
        return mdsInfo;
    }

    public byte[] getThumbnail(SliceParameter sliceParameter) {
        byte[] data = null;
        HashCode code = Hashing.md5().hashString(sliceParameter.toString(), Charset.defaultCharset());
        if (mdsFileCache != null) {
            data = (byte[]) mdsFileCache.get(code);
            if (data == null) {
                MDSFile mdsFile = generateMDSFile(sliceParameter.getPath());
                if (mdsFile != null) {
                    data = mdsFile.getThumbnail(sliceParameter.getThumbnailWidth());
                    if (data != null) {
                        mdsFileCache.set(code, data);
                    }
                }
            }
        }
        return data;
    }

    @Autowired
    public MDSFileFactory(Configuration configuration) {
        this.configuration = configuration;
        mdsFileCache = new GuanvaCacheGetter(configuration.getMDSFileCacheProperties());
        sliceCache = new GuanvaCacheGetter(configuration.getSliceCacheProperties());
    }
}
