package net.sunxu.website.auth.service.bo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.sunxu.website.user.dto.UserDetailsDTO;
import net.sunxu.website.user.dto.UserState;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsImpl implements UserDetails {

    private final UserDetailsDTO user;

    private final List<? extends GrantedAuthority> authorities;

    private final String userName;

    public UserDetailsImpl(UserDetailsDTO user) {
        this.user = user;
        userName = "USER_" + user.getId();
        this.authorities = user.getRoles() == null ? Collections.emptyList() :
                user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public Long getId() {
        return user.getId();
    }

    public UserDetailsDTO getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getUserState() == UserState.NORMAL;
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
