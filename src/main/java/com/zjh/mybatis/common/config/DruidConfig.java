package com.zjh.mybatis.common.config;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author xcy
 * @创建日期 2018年6月7日 Druid集成
 */
@Configuration
@MapperScan(basePackages = "com.zjh.mybatis.**.dao", sqlSessionTemplateRef = "druidSqlSessionTemplate")
public class DruidConfig {

	/*
	 * datasource 相关
	 */
	@Value("${spring.datasource.url}")
	private String dbUrl;
	@Value("${spring.datasource.username}")
	private String username;
	@Value("${spring.datasource.password}")
	private String password;
	@Value("${spring.datasource.driver-class-name}")
	private String driverClassName;
	@Value("${spring.datasource.initialSize}")
	private int initialSize;
	@Value("${spring.datasource.minIdle}")
	private int minIdle;
	@Value("${spring.datasource.maxActive}")
	private int maxActive;
	@Value("${spring.datasource.maxWait}")
	private int maxWait;
	@Value("${spring.datasource.timeBetweenEvictionRunsMillis}")
	private int timeBetweenEvictionRunsMillis;
	@Value("${spring.datasource.minEvictableIdleTimeMillis}")
	private int minEvictableIdleTimeMillis;
	@Value("${spring.datasource.validationQuery}")
	private String validationQuery;
	@Value("${spring.datasource.testWhileIdle}")
	private boolean testWhileIdle;
	@Value("${spring.datasource.testOnBorrow}")
	private boolean testOnBorrow;
	@Value("${spring.datasource.testOnReturn}")
	private boolean testOnReturn;
	@Value("${spring.datasource.poolPreparedStatements}")
	private boolean poolPreparedStatements;
	@Value("${spring.datasource.maxPoolPreparedStatementPerConnectionSize}")
	private int maxPoolPreparedStatementPerConnectionSize;
	@Value("${spring.datasource.filters}")
	private String filters;
	@Value("${spring.datasource.connectionProperties}")
	private String connectionProperties;
	@Value("${druid.dbType}")
	private String dbType;
	@Value("${druid.stat-filter.mergeSql}")
	private boolean mergeSql;
	@Value("${druid.stat-filter.slowSqlMillis}")
	private long slowSqlMillis;
	@Value("${druid.stat-filter.logSlowSql}")
	private boolean logSlowSql;
	@Value("${druid.wall-filter.dir}")
	private String dir;
	@Value("${druid.wall-filter.limitZeroAllow}")
	private boolean limitZero;
	@Value("${druid.wall-filter.multiStatementAllow}")
	private boolean multiStatementAllow;
	@Value("${druid.wall-filter.metadataAllow}")
	private boolean metadataAllow;
	@Value("${druid.wall-filter.wrapAllow}")
	private boolean wrapAllow;
	
	/**
	 * 注册数据源
	 */
	@Bean(name="druidDataSource")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource druidDataSource() {
		DruidDataSource datasource = new DruidDataSource();
		datasource.setUrl(this.dbUrl);
		datasource.setUsername(username);
		datasource.setPassword(password);
		datasource.setDriverClassName(driverClassName);

	    //configuration
	    datasource.setInitialSize(initialSize);
	    datasource.setMinIdle(minIdle);
	    datasource.setMaxActive(maxActive);
	    datasource.setMaxWait(maxWait);
	    datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
	    datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
	    datasource.setValidationQuery(validationQuery);
	    datasource.setTestWhileIdle(testWhileIdle);
	    datasource.setTestOnBorrow(testOnBorrow);
	    datasource.setTestOnReturn(testOnReturn);
	    datasource.setPoolPreparedStatements(poolPreparedStatements);
	    datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
	    // 添加过滤器
	    List<Filter> filterList = new ArrayList<Filter>();
	    filterList.add(getStatFilter());
	    filterList.add(getWallFilter());
	    datasource.setProxyFilters(filterList);
	    datasource.setConnectionProperties(connectionProperties);
	    return datasource;
	}
	
	@Bean(name="druidSqlSessionFactory")
	public SqlSessionFactory druidSqlSessionFactory(@Qualifier("druidDataSource") DataSource dataSource) throws Exception {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource); 
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try {
			bean.setMapperLocations(resolver.getResources("classpath:com/zjh/mybatis/**/dao/*.xml"));
			return bean.getObject();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	@Bean
	public SqlSessionTemplate druidSqlSessionTemplate(@Qualifier("druidSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
		SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactory);
		return template;
	}

	/**
	 * 注册Servlet
	 */
	@Bean
	public ServletRegistrationBean druidServlet() {
		ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(),
				"/druid/*");
		// 白名单：
		servletRegistrationBean.addInitParameter("allow", "127.0.0.1");
		// IP黑名单 (存在共同时，deny优先于allow) : 如果满足deny的话提示:Sorry, you are not
		// permitted to view this page.
		servletRegistrationBean.addInitParameter("deny", "192.168.1.73");
		// 登录查看信息的账号密码.
		servletRegistrationBean.addInitParameter("loginUsername", "clj");
		servletRegistrationBean.addInitParameter("loginPassword", "123456");
		// 是否能够重置数据.
		servletRegistrationBean.addInitParameter("resetEnable", "false");
		return servletRegistrationBean;
	}

	@Bean
	public FilterRegistrationBean filterRegistrationBean() {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		filterRegistrationBean.setFilter(new WebStatFilter());
		filterRegistrationBean.addUrlPatterns("/*");
		filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
		return filterRegistrationBean;
	}
	
	/**
	 * statfilter
	 */
	@Bean
	public StatFilter getStatFilter() {
		StatFilter statFilter = new StatFilter();
		statFilter.setMergeSql(mergeSql);// 只有参数不同的sql合并统计
		statFilter.setSlowSqlMillis(slowSqlMillis);// 判断为慢sql的执行时间
		statFilter.setDbType(dbType);// 数据库类型
		statFilter.setLogSlowSql(logSlowSql);// 是否记录慢sql
		return statFilter;
	}
	
	/**
	 * wallconfig
	 */
	@Bean
	public WallConfig getWallConfig(){
		WallConfig config = new WallConfig();
		config.setDir(dir);// 配置目录
		config.setLimitZeroAllow(limitZero);// 是否允许limit 0
		config.setMultiStatementAllow(multiStatementAllow);// 是否允许一次执行多条语句
		config.setMetadataAllow(metadataAllow);// 是否允许调用Connection.getMetadata方法
		config.setWrapAllow(wrapAllow);// 是否允许调用Connection/Statement/ResultSet的isWrapFor和unwrap方法
		return config;
	}
	
	/**
	 * wallfilter
	 */
	@Bean
	public WallFilter getWallFilter(){
		WallFilter wallFilter = new WallFilter();
		wallFilter.setDbType(dbType);// 数据库类型
		wallFilter.setConfig(getWallConfig());// 配置信息
		return wallFilter;
	}
}
