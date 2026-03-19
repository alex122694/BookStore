package bookstore.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "club_categories")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ClubCategoriesBean {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private Integer categoryId;

	@Column(name = "category_name", nullable = false, length = 50)
	private String categoryName;
}
