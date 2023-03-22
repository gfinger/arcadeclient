package org.makkiato.arcadeclient;

import lombok.Builder;

@Builder
record Address(String street, String zip, String city, String phone) {
}
