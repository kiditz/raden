package com.slerp.ecomerce.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Size;
import javax.persistence.GenerationType;
import javax.persistence.Basic;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.math.BigDecimal;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(name = "product")
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@XmlAccessorType(XmlAccessType.NONE)
public class Product {

	@Id
	@Column(name = "product_id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRODUCT_SEQ")
	@SequenceGenerator(name = "PRODUCT_SEQ", sequenceName = "product_seq", initialValue = 1)
	@Size(min = 1, max = 19)
	private Long productId;
	@Column(name = "product_name")
	@Basic(optional = false)
	@NotNull(message = "com.slerp.ecomerce.entity.Product.productName")
	@Size(min = 1, max = 60)
	private String productName;
	@Column(name = "product_description")
	private String productDescription;
	@Column(name = "product_last_update")
	@Size(min = 1, max = 29)
	private Date productLastUpdate;
	@Column(name = "product_price")
	@Size(min = 1, max = 9)
	private BigDecimal productPrice;
	@ManyToOne(optional = false)
	@JoinColumn(name = "category_id", referencedColumnName = "category_id")
	private Category categoryId;

	@JsonProperty
	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	@JsonProperty
	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	@JsonProperty
	public String getProductDescription() {
		return productDescription;
	}

	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

	@JsonProperty
	public Date getProductLastUpdate() {
		return productLastUpdate;
	}

	public void setProductLastUpdate(Date productLastUpdate) {
		this.productLastUpdate = productLastUpdate;
	}

	@JsonProperty
	public BigDecimal getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(BigDecimal productPrice) {
		this.productPrice = productPrice;
	}

	@JsonProperty
	public Category getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Category categoryId) {
		this.categoryId = categoryId;
	}
}