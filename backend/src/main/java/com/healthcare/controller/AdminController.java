package com.healthcare.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.healthcare.dto.DrugDto;
import com.healthcare.entity.DoctorProfile;
import com.healthcare.entity.User;
import com.healthcare.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@SaCheckRole("ADMIN")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public Page<AdminService.UserVo> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return adminService.listUsers(page, size, keyword, status);
    }

    @GetMapping("/users/{id}")
    public AdminService.UserVo getUser(@PathVariable Long id) {
        return adminService.getUser(id);
    }

    @PutMapping("/users/{id}/status")
    public void updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        adminService.updateUserStatus(id, User.UserStatus.valueOf(status));
    }

    @GetMapping("/doctors")
    public Page<AdminService.DoctorVo> listDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String auditStatus) {
        return adminService.listDoctors(page, size, keyword, auditStatus);
    }

    @GetMapping("/doctors/{id}")
    public AdminService.DoctorVo getDoctor(@PathVariable Long id) {
        return adminService.getDoctor(id);
    }

    @PutMapping("/doctors/{id}/audit")
    public void updateDoctorAudit(@PathVariable Long id, @RequestParam String status) {
        adminService.updateDoctorAudit(id, DoctorProfile.AuditStatus.valueOf(status));
    }

    @PutMapping("/doctors/{id}/status")
    public void updateDoctorStatus(@PathVariable Long id, @RequestParam String status) {
        AdminService.DoctorVo d = adminService.getDoctor(id);
        adminService.updateDoctorStatus(d.getUserId(), User.UserStatus.valueOf(status));
    }

    @GetMapping("/drugs")
    public Page<com.healthcare.entity.Drug> listDrugs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return adminService.listDrugs(page, size, keyword);
    }

    @PostMapping("/drugs")
    public com.healthcare.entity.Drug createDrug(@RequestBody DrugDto dto) {
        return adminService.createDrug(dto);
    }

    @PutMapping("/drugs/{id}")
    public com.healthcare.entity.Drug updateDrug(@PathVariable Long id, @RequestBody DrugDto dto) {
        return adminService.updateDrug(id, dto);
    }

    @GetMapping("/stats/overview")
    public Map<String, Object> statsOverview() {
        return adminService.statsOverview();
    }

    @GetMapping("/stats/doctor-consult")
    public Map<String, Object> doctorConsultStats(@RequestParam(defaultValue = "7") int days) {
        return adminService.doctorConsultStats(days);
    }
}
