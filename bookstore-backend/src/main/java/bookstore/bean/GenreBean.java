package bookstore.bean;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "genres")
public class GenreBean {
	@Id
	@Column(name = "genre_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer genreId;

	@Column(name = "genre_name")
	private String genreName;

	@ManyToMany(mappedBy = "genres")
	@JsonIgnore
	private Set<BooksBean> books = new HashSet<>();

	// Constructor----
	public GenreBean() {
		super();
	}

	public GenreBean(Integer genreId, String genreName) {
		super();
		this.genreId = genreId;
		this.genreName = genreName;
	}

	// Getter/Setter----
	public Integer getGenreId() {
		return genreId;
	}

	public void setGenreId(Integer genreId) {
		this.genreId = genreId;
	}

	public String getGenreName() {
		return genreName;
	}

	public void setGenreName(String genreName) {
		this.genreName = genreName;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null || getClass() != object.getClass())
			return false;
		GenreBean that = (GenreBean) object;
		return Objects.equals(genreId, that.genreId);

	}

	@Override
	public int hashCode() {
		return Objects.hash(genreId);
	}
}
