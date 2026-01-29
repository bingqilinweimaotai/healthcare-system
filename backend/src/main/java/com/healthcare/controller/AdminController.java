package com.healthcare.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.healthcare.common.Result;
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
    public Result<Page<AdminService.UserVo>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.ok(adminService.listUsers(page, size, keyword, status));
    }

    @GetMapping("/users/{id}")
    public Result<AdminService.UserVo> getUser(@PathVariable Long id) {
        return Result.ok(adminService.getUser(id));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        adminService.updateUserStatus(id, User.UserStatus.valueOf(status));
        return Result.ok();
    }

    @GetMapping("/doctors")
    public Result<Page<AdminService.DoctorVo>> listDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String auditStatus) {
        return Result.ok(adminService.listDoctors(page, size, keyword, auditStatus));
    }

    @GetMapping("/doctors/{id}")
    public Result<AdminService.DoctorVo> getDoctor(@PathVariable Long id) {
        return Result.ok(adminService.getDoctor(id));
    }

    @PutMapping("/doctors/{id}/audit")
    public Result<Void> updateDoctorAudit(@PathVariable Long id, @RequestParam String status) {
        adminService.updateDoctorAudit(id, DoctorProfile.AuditStatus.valueOf(status));
        return Result.ok();
    }

    @PutMapping("/doctors/{id}/status")
    public Result<Void> updateDoctorStatus(@PathVariable Long id, @RequestParam String status) {
        AdminService.DoctorVo d = adminService.getDoctor(id);
        adminService.updateDoctorStatus(d.getUserId(), User.UserStatus.valueOf(status));
        return Result.ok();
    }

    @GetMapping("/drugs")
    public Result<Page<com.healthcare.entity.Drug>> listDrugs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.listDrugs(page, size, keyword));
    }

    @PostMapping("/drugs")
    public Result<com.healthcare.entity.Drug> createDrug(@RequestBody DrugDto dto) {
        return Result.ok(adminService.createDrug(dto));
    }

    @PutMapping("/drugs/{id}")
    public Result<com.healthcare.entity.Drug> updateDrug(@PathVariable Long id, @RequestBody DrugDto dto) {
        return Result.ok(adminService.updateDrug(id, dto));
    }

    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> statsOverview() {
        return Result.ok(adminService.statsOverview());
    }

    @GetMapping("/stats/doctor-consult")
    public Result<Map<String, Object>> doctorConsultStats(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(adminService.doctorConsultStats(days));
    }
}
