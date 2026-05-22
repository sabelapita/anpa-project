package es.udc.anpaproject.backend.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "User",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"userName"})})
public class User {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "userName", nullable = false, unique = true, columnDefinition = "VARCHAR(60) COLLATE latin1_bin")
	private String userName;

	@Column(nullable = false, length = 60)
	private String password;

	@Column(nullable = false, length = 60)
	private String firstName;

    @Column(nullable = false, length = 60)
	private String lastName;

    @Column(nullable = false, length = 60)
	private String email;

	@Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
	private RoleType role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private MembershipStatus membershipStatus;

	public User() {}

	public User(String userName, String password, String firstName, String lastName, String email) {

		this.userName = userName;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	    this.role = RoleType.USER;
		this.membershipStatus = MembershipStatus.NON_MEMBER;
		
	}

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public RoleType getRole() {
		return role;
	}

	public void setRole(RoleType role) {
		this.role = role;
	}

	public MembershipStatus getMembershipStatus() {
		return membershipStatus;
	}

	public void setMembershipStatus(MembershipStatus membershipStatus) {
		this.membershipStatus = membershipStatus;
	}

}
