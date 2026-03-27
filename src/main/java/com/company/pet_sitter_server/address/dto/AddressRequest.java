package com.company.pet_sitter_server.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddressRequest {

    @NotBlank
    public String label;

    @NotBlank
    public String addressLine;

    @NotBlank
    public String city;

    @NotBlank
    public String province;

    @NotBlank
    public String postalCode;

    @NotNull
    public Long userId;
}