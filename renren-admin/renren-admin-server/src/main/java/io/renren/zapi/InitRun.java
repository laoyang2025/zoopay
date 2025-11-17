package io.renren.zapi;

import io.renren.zadmin.dao.ZWarningDao;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class InitRun implements CommandLineRunner {

    @Resource
    private ZWarningDao zWarningDao;

    @Override
    public void run(String... args) throws Exception {
    }

}
