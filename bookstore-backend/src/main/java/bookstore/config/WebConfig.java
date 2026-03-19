package bookstore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry; // 新增這行
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private LoginInterceptor loginInterceptor;

	@Autowired
	private bookstore.util.JwtInterceptor jwtInterceptor;

	@Value("${file.upload-dir}")
	private String uploadDir;

	@Override
	public void addCorsMappings(@NonNull CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOriginPatterns("http://localhost:5173", "https://*.ecpay.com.tw")// 允許這些網址讀取後端回傳的 Response
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowCredentials(true)
				.allowedHeaders("*");
	}

	@Override
	public void addInterceptors(@NonNull InterceptorRegistry registry) {
		if (loginInterceptor != null) {
			registry.addInterceptor(loginInterceptor)
					.addPathPatterns("/users/**")
					.excludePathPatterns("/login", "/api/login")
					.excludePathPatterns("/api/**")
					.excludePathPatterns("/logout")
					.excludePathPatterns("/get-test-token")

					.excludePathPatterns("/static/**", "/css/**", "/js/**", "/images/**", "/proof/**");
		}

		if (jwtInterceptor != null) {
			registry.addInterceptor(jwtInterceptor)
					.addPathPatterns("/cart/api/**")
					.addPathPatterns("/orders/**")
					.addPathPatterns("/api/clubs/**")
					.excludePathPatterns("/api/books/getAllBooks",
							"/api/books/isbnCheck",
							"/api/books/genres",
							"/api/book/delete/**")
					//
					.excludePathPatterns("/orders/ecpay/**")
					.excludePathPatterns("/orders/ecpay/**")
					.excludePathPatterns("/api/clubs/allClubs")
					.excludePathPatterns("/api/clubs/clubs/**")
					.excludePathPatterns("/uploads/**");
			
			// .addPathPatterns("/api/books**")
			// .excludePathPatterns("/api/books/getAllBooks",
			// "api/books/getBook/**",
			// "api/books/isbnCheck",
			// "api/books/genres");

		}
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/upload-images/**")
				.addResourceLocations("file:" + uploadDir);
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations("file:///C:/uploads/");
		registry.addResourceHandler("/proof/**")
				.addResourceLocations("file:///C:/uploads/proof/proof/");
	}

}