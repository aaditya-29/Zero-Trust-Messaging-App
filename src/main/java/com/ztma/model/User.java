package com.ztma.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.ElementCollection;

@Document
public class User implements UserDetails {

	@Id
	private String id;
	private String name;
	private String email;
	private String password;
	private boolean verified;
	private String publicKey;
	private String privateKey;
	private String verificationCode;
	@ElementCollection
	private List<String> knownDevices = new ArrayList<>();

	@ElementCollection
	private Set<String> knownIps = new HashSet<>();

	// ===== Getters and Setters =====

	public List<String> getKnownDevices() {
		return knownDevices;
	}

	public void setKnownDevices(List<String> knownDevices) {
		this.knownDevices = knownDevices;
	}

	
	public Set<String> getKnownIps() {
		return knownIps;
	}

	public void setKnownIps(Set<String> knownIps) {
		this.knownIps = knownIps;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	// ===== Spring Security UserDetails Methods =====

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
	}

//	// implement UserDetails methods properly
//	@Override
//	public Collection<? extends GrantedAuthority> getAuthorities() {
//		return null;
//	}

	@Override
	public String getUsername() {
		return email;
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
		return verified;
	}

}
