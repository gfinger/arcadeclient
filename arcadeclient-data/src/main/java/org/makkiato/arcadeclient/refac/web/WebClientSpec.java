package org.makkiato.arcadeclient.refac.web;

public record WebClientSpec(String host, Integer port, boolean ha, boolean leader, boolean replica) {
    
}
