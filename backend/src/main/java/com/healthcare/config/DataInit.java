package com.healthcare.config;

import com.healthcare.entity.Drug;
import com.healthcare.entity.User;
import com.healthcare.mapper.DrugMapper;
import com.healthcare.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataInit implements CommandLineRunner {

    private final UserMapper userMapper;
    private final DrugMapper drugMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userMapper.selectByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("管理员");
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(User.UserStatus.NORMAL);
            userMapper.insert(admin);
        }
        if (drugMapper.count() == 0) {
            String[][] drugs = {
                    {"阿莫西林胶囊", "0.5g*24粒", "盒", "口服，一次0.5g，一日3次"},
                    {"布洛芬缓释胶囊", "0.3g*20粒", "盒", "口服，一次1粒，一日2次"},
                    {"复方甘草片", "50片", "瓶", "口服，一次3-4片，一日3次"},
            };
            for (String[] d : drugs) {
                Drug drug = new Drug();
                drug.setName(d[0]);
                drug.setSpec(d[1]);
                drug.setUnit(d[2]);
                drug.setUsageInstruction(d[3]);
                drug.setStatus(Drug.DrugStatus.ACTIVE);
                drugMapper.insert(drug);
            }
        }
    }
}
