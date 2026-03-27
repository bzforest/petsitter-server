package com.company.pet_sitter_server.address.service;

import com.company.pet_sitter_server.address.dto.AddressRequest;
import com.company.pet_sitter_server.address.dto.AddressResponse;
import com.company.pet_sitter_server.address.entity.Address;
import com.company.pet_sitter_server.address.repository.AddressRepository;
import com.company.pet_sitter_server.user.entity.User;
import com.company.pet_sitter_server.user.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository repo;
    private final UserRepository userRepo;

    public AddressService(AddressRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    // ✅ CREATE
    public AddressResponse create(AddressRequest req) {

        User user = userRepo.findById(req.userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = new Address();
        address.setLabel(req.label);
        address.setAddressLine(req.addressLine);
        address.setCity(req.city);
        address.setProvince(req.province);
        address.setPostalCode(req.postalCode);
        address.setUser(user);

        repo.save(address);

        return mapToResponse(address);
    }

    // ✅ GET ALL BY USER
    public List<AddressResponse> getByUser(Long userId) {

        return repo.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ✅ GET BY ID
    public AddressResponse getById(Long id) {

        Address address = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        return mapToResponse(address);
    }

    // ✅ UPDATE
    public AddressResponse update(Long id, AddressRequest req) {

        Address address = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.setLabel(req.label);
        address.setAddressLine(req.addressLine);
        address.setCity(req.city);
        address.setProvince(req.province);
        address.setPostalCode(req.postalCode);

        repo.save(address);

        return mapToResponse(address);
    }

    // ✅ DELETE
    public void delete(Long id) {

        if (!repo.existsById(id)) {
            throw new RuntimeException("Address not found");
        }

        repo.deleteById(id);
    }

    private AddressResponse mapToResponse(Address address) {
        AddressResponse res = new AddressResponse();
        res.id = address.getId();
        res.label = address.getLabel();
        res.addressLine = address.getAddressLine();
        res.city = address.getCity();
        res.province = address.getProvince();
        res.postalCode = address.getPostalCode();
        res.userId = address.getUser().getId();
        return res;
    }
}