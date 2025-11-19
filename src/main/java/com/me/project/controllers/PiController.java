package com.me.project.controllers;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.me.project.entity.PiDevice;
import com.me.project.repository.PiDeviceRepository;

@RestController
@RequestMapping("/api/pi")
@CrossOrigin(origins = "*")
public class PiController {

    private final PiDeviceRepository repo;

    public PiController(PiDeviceRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @PostMapping("/register")
    public String registerPi(@RequestBody PiDevice newPi) {

        newPi.setOnline(true);
        newPi.setLastSeen(LocalDateTime.now());

        repo.save(newPi);
        System.out.println("Ragistered new Pi: " + newPi.getDeviceId());
        return "Pi registered: " + newPi.getDeviceId();
    }
    @PostMapping("/heartbeat/{deviceId}")
    public String heartbeat(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> payload) {

        PiDevice pi = repo.findById(deviceId).orElse(null);

        if (pi == null) {
            return "Pi not found!";
        }

        String ip = payload.get("ip");

        pi.setIpAddress(ip);
        pi.setOnline(true);
        pi.setLastSeen(LocalDateTime.now());
        
        repo.save(pi);
        	System.out.println("Hartbeat received from Pi: " + deviceId + " with IP: " + ip);
        return "Heartbeat updated!";
    }
}

