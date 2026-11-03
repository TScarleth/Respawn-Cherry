package com.respawncherry.foro.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class ForoPrincipal implements UserDetails {

    private final Long id;
    private final String correo;
    private final String rol;

    public ForoPrincipal(Long id, String correo, String rol) {
        this.id = id;
        this.correo = correo;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public boolean esAdmin() {
        return "ADMIN".equals(rol);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
