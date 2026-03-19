package bookstore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import bookstore.bean.GenreBean;
import bookstore.util.DBUtil;

public class GenresDao {

	public List<GenreBean> getAllGenres() {
		String sql = "select * from genres";
		List<GenreBean> genresList = new ArrayList<GenreBean>();

		try (Connection connection = DBUtil.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Integer genreId = resultSet.getInt("genre_id");
				String genreName = resultSet.getString("genre_name");
				GenreBean genre = new GenreBean(genreId, genreName);
				genresList.add(genre);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return genresList;
	}
}
