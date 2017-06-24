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
import java.util.List;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;

@Entity
@Table(name = "category")
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@XmlAccessorType(XmlAccessType.NONE)
public class Category {

	@Id
	@Column(name = "category_id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CATEGORY_SEQ")
	@SequenceGenerator(name = "CATEGORY_SEQ", sequenceName = "category_seq", initialValue = 1)
	@Size(min = 1, max = 19)
	private Long categoryId;
	@Column(name = "category_name")
	@Size(min = 1, max = 60)
	private String categoryName;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "categoryId")
	private List<Product> productList;

	@JsonProperty
	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	@JsonProperty
	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public List<Product> getProductList() {
		return productList;
	}

	public void setProductList(List<Product> productList) {
		this.productList = productList;
	}
}