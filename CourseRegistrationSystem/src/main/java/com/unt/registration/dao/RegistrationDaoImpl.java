package com.unt.registration.dao;

import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.unt.registration.service.RegistrationServiceImpl;
import com.unt.registration.util.Course;
import com.unt.registration.util.Department;
import com.unt.registration.util.EnrollObject;
import com.unt.registration.util.Enrollment;
import com.unt.registration.util.Grade;
import com.unt.registration.util.Payment;
import com.unt.registration.util.SelectCriteria;
import com.unt.registration.util.SwapCourse;
import com.unt.registration.util.User;

@Repository
public class RegistrationDaoImpl implements RegistrationDao {

	@Autowired
	JdbcTemplate jdbcTemplate;
	RegistrationServiceImpl registrationServiceImpl;

	// User Validation
	@Override
	public User userValidate(String id) {
		// TODO Auto-generated method stub
		String sql = "select * from \"Registration DB\".\"Userdetails\" where id=?";
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, new BeanPropertyRowMapper<>(User.class));
	}

	// Checking whether User exists or not
	@Override
	public int userExists(String id) {
		// TODO Auto-generated method stub
		String sql = "SELECT COUNT(*) FROM \"Registration DB\".\"Userdetails\" where id=?";
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, Integer.class);
	}

	// Checking User id provided by university or not
	@Override
	public int userIdProvided(String id) {
		// TODO Auto-generated method stub
		String sql = "SELECT COUNT(*) FROM \"Registration DB\".\"Universitydetails\" where id=?";
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, Integer.class);
	}

	// user sign up
	@Override
	public int signup(User user) {
		// TODO Auto-generated method stub
		String sql = "INSERT INTO \"Registration DB\".\"Userdetails\" values(?,?,?,?,?,?,?,?)";
		Object[] args = { user.getId(), user.getFirstName(), user.getLastName(), user.getEmail().toLowerCase(),
				user.getMobile(), user.getDeptId(), 0, user.getPassword() };
		int[] argTypes = { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC, Types.VARCHAR,
				Types.NUMERIC, Types.VARCHAR };
		return jdbcTemplate.update(sql, args, argTypes);
	}

	// Get all departments
	@Override
	public List<Department> fetchAllDepartments() {
		// TODO Auto-generated method stub
		String sql = "SELECT * FROM \"Registration DB\".\"Departments\"";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<Department>(Department.class));
	}

	// Get user email id
	@Override
	public String getEmail(String id) {
		String sql = "SELECT email FROM \"Registration DB\".\"Userdetails\" where id=?";
		return jdbcTemplate.queryForObject(sql, new Object[] { id }, String.class);
	}

	// Reset Password
	@Override
	public int resetPassword(User user) {
		// TODO Auto-generated method stub
		String sql = "UPDATE \"Registration DB\".\"Userdetails\" SET password=? where id=?";
		Object[] args = { user.getPassword(), user.getId() };
		int[] argTypes = { Types.VARCHAR, Types.VARCHAR };
		return jdbcTemplate.update(sql, args, argTypes);
	}

	// Get current semester
	public String getCurrentSemester() {
		String currentSemester;
		if ((Calendar.getInstance().get(Calendar.MONTH)) >= 1 && (Calendar.getInstance().get(Calendar.MONTH)) <= 4)
			currentSemester = "Spring";
		else if ((Calendar.getInstance().get(Calendar.MONTH)) >= 5 && (Calendar.getInstance().get(Calendar.MONTH)) <= 7)
			currentSemester = "Summer";
		else
			currentSemester = "Fall";
		return currentSemester;
	}

	// Get all courses
	@Override
	public List<Course> getCourses(SelectCriteria selectCriteria) {
		// TODO Auto-generated method stub

		String sql = "SELECT * FROM \"Registration DB\".\"Courses\" where degree=? and \"deptId\"=? and \"isActive\"=? and semester=? and year=?";
		Object[] args = { selectCriteria.getDegree(), selectCriteria.getDeptId(), true, this.getCurrentSemester(),
				Calendar.getInstance().get(Calendar.YEAR) };
		return jdbcTemplate.query(sql, args, new BeanPropertyRowMapper<Course>(Course.class));
	}

	// Find Course
	@Override
	public Course findCourse(String courseId) {
		// TODO Auto-generated method stub
		String sql = "SELECT * FROM \"Registration DB\".\"Courses\" where \"courseId\"=? and semester=? and year=?";
		return jdbcTemplate.queryForObject(sql,
				new Object[] { courseId, this.getCurrentSemester(), Calendar.getInstance().get(Calendar.YEAR) },
				new BeanPropertyRowMapper<>(Course.class));
	}

	// Fetch all enrolled courses
	public List<Course> fetchEnrolledCourses(User user) {
		String sql = "select * from \"Registration DB\".\"Enrollments\" JOIN \"Registration DB\".\"Courses\" ON \"Registration DB\".\"Enrollments\".\"courseId\"=\"Registration DB\".\"Courses\".\"courseId\" where \"Registration DB\".\"Enrollments\".id=? and \"Registration DB\".\"Enrollments\".semester=? and \"Registration DB\".\"Enrollments\".year=?";
		Object[] args = { user.getId(), this.getCurrentSemester(), Calendar.getInstance().get(Calendar.YEAR) };
		return jdbcTemplate.query(sql, args, new BeanPropertyRowMapper<Course>(Course.class));
	}

	// Drop Course
	@Override
	public int dropCourse(EnrollObject enrollObject) {
		// TODO Auto-generated method stub
		String sql = "DELETE FROM \"Registration DB\".\"Enrollments\" WHERE id = '" + enrollObject.getUserId()
				+ "' AND \"courseId\" = '" + enrollObject.getCourseId() + "' and semester='" + this.getCurrentSemester()
				+ "'AND year=" + Calendar.getInstance().get(Calendar.YEAR);
		return jdbcTemplate.update(sql);
	}

	// Decrease Strength
	@Override
	public int decreaseStrength(String id) {
		String sql = "update \"Registration DB\".\"Courses\" set strength = strength-1 where \"courseId\"='" + id
				+ "' and semester='" + this.getCurrentSemester() + "'AND year="
				+ Calendar.getInstance().get(Calendar.YEAR);
		return jdbcTemplate.update(sql);
	}

	// Check course Strength
	public int checkStrength(String courseId) {
		String sql = "SELECT count(*)   FROM \"Registration DB\".\"Courses\" where \"Registration DB\".\"Courses\".\"strength\"  < \"Registration DB\".\"Courses\".\"courseMaxStrength\" AND \"Registration DB\".\"Courses\".\"courseId\"='"
				+ courseId + "'";
		return jdbcTemplate.queryForObject(sql, Integer.class);
	}

	// Increase Strength
	@Override
	public int increaseStrength(String courseId) {
		String sql = "update \"Registration DB\".\"Courses\" set strength = strength+1 where \"courseId\"='" + courseId
				+ "'";
		return jdbcTemplate.update(sql);

	}

	// Enroll into course
	@Override
	public int enroll(EnrollObject enrollObject) {
		// TODO Auto-generated method stub
		String sql = "INSERT INTO  \"Registration DB\".\"Enrollments\" values(?,?,?,?,?)";
		Object[] args = { enrollObject.getUserId(), enrollObject.getCourseId(), this.getCurrentSemester(),
				Calendar.getInstance().get(Calendar.YEAR), null };
		int[] argTypes = { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC, Types.CHAR };
		return jdbcTemplate.update(sql, args, argTypes);

	}

	// check course prerequisites
	@Override
	public int checkPrerequisites(String userId, String courseId) {
		String sql = "SELECT COUNT(*) FROM \"Registration DB\".\"Enrollments\" where id='" + userId
				+ "' AND \"courseId\"='" + courseId + "' AND grade is not NULL";
		return jdbcTemplate.queryForObject(sql, Integer.class);

	}

	// get prerequisites
	@Override
	public String getPrerequisites(String courseId) {
		// TODO Auto-generated method stub
		String sql = "SELECT \"prerequisiteId\" FROM \"Registration DB\".\"Prerequisites\" where \"courseId\"='"
				+ courseId + "'";
		return jdbcTemplate.queryForObject(sql, String.class);

	}

	// check prerequisites exists or not
	@Override
	public int ifPrerequisitesExist(String courseId) {
		// TODO Auto-generated method stub
		String sql = "SELECT count(*) FROM \"Registration DB\".\"Prerequisites\" where \"courseId\"='" + courseId + "'";
		return jdbcTemplate.queryForObject(sql, Integer.class);

	}

	// Post payment
	@Override
	public int postPayment(Payment payment, String paymentId, String strDate) {
		// TODO Auto-generated method stub
		try {
			if (payment.getPaymentAmount() > 0.0) {
				System.out.println(payment.getPaymentAmount());
				String sql = "INSERT INTO \"Registration DB\".\"Payments\" values(?,?,?,?)";
				Object[] args = { payment.getId(), paymentId, strDate, payment.getPaymentAmount() };
				int[] argTypes = { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC };
				return jdbcTemplate.update(sql, args, argTypes);
			} else
				return 2;
		} catch (Exception e) {
			return 3;
		}
	}

	// Get past payments
	@Override
	public List<Payment> pastPayments(User user) {
		// TODO Auto-generated method stub
		try {
			String sql = "SELECT * FROM \"Registration DB\".\"Payments\" where id=?";
			return (List<Payment>) jdbcTemplate.query(sql, new Object[] { user.getId() },
					new BeanPropertyRowMapper<Payment>(Payment.class));
		} catch (Exception e) {

			return null;
		}

	}

	// View Grades
	@Override
	public List<Grade> viewGrades(User user) {
		// TODO Auto-generated method stub
		String sql = "select * from \"Registration DB\".\"Enrollments\" JOIN \"Registration DB\".\"Courses\" ON \"Registration DB\".\"Enrollments\".\"courseId\"=\"Registration DB\".\"Courses\".\"courseId\" where \"Registration DB\".\"Enrollments\".id=? and grade IS NOT NULL";

		return (List<Grade>) jdbcTemplate.query(sql, new Object[] { user.getId() },
				new BeanPropertyRowMapper<Grade>(Grade.class));
	}

	// Get Past payments total amount
	@Override
	public int pastPaymentsAmount(Payment payment) {
		// TODO Auto-generated method stub


		try {
			String sql = "select sum(\"paymentAmount\") from \"Registration DB\".\"Payments\" where id='"
					+ payment.getId() + "'";
			return jdbcTemplate.queryForObject(sql, Integer.class);
		} catch (Exception e) {
			return 0;
		}


		

	}

	// Total due amount
	@Override
	public int totalAmount(Payment payment) {
		// TODO Auto-generated method stub

		String sql="select sum(amount) from \"Registration DB\".\"Enrollments\" JOIN \"Registration DB\".\"Courses\" ON \"Registration DB\".\"Enrollments\".\"courseId\"=\"Registration DB\".\"Courses\".\"courseId\" where \"Registration DB\".\"Enrollments\".id='"+payment.getId()+"'";
		return jdbcTemplate.queryForObject(sql,  Integer.class);

	}

	// Mandatory courses done
	@Override
	public List<Course> mandatoryCoursesDone(User user) {
		// TODO Auto-generated method stub
		String sql = "select * from \"Registration DB\".\"Enrollments\" JOIN \"Registration DB\".\"Courses\" ON"
				+ " \"Registration DB\".\"Enrollments\".\"courseId\"=\"Registration DB\".\"Courses\".\"courseId\" where "
				+ "\"Registration DB\".\"Courses\".\"isMandatory\"=true AND \"Registration DB\".\"Enrollments\".id='"
				+ user.getId() + "'" + "AND \"Registration DB\".\"Courses\".\"deptId\"='" + user.getDeptId() + "'";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<Course>(Course.class));
	}

	// Mandatory courses Not done
	@Override
	public List<Course> mandatoryCoursesNotDone(User user) {
		// TODO Auto-generated method stub
		String sql = "select * from  \"Registration DB\".\"Courses\"  \r\n" + " \r\n"
				+ "where \"Registration DB\".\"Courses\".\"isMandatory\"=true AND (\r\n"
				+ "														  \"Registration DB\".\"Courses\".\"deptId\"=?) and \"Courses\".\"courseId\"\r\n"
				+ "		NOT IN(select \"Courses\".\"courseId\" from \"Registration DB\".\"Enrollments\" JOIN \r\n"
				+ "			   \"Registration DB\".\"Courses\" ON \"Registration DB\".\"Enrollments\".\"courseId\"=\"Registration DB\".\"Courses\".\"courseId\" where \r\n"
				+ "				\"Registration DB\".\"Courses\".\"isMandatory\"=true AND \"Registration DB\".\"Enrollments\".id=?);";

		return jdbcTemplate.query(sql, new Object[] { user.getDeptId(), user.getId() },
				new BeanPropertyRowMapper<Course>(Course.class));
	}

	// Fetch Available courses for student
	@Override
	public List<Course> fetchAvailableCourses(SelectCriteria selectCriteria) {
		// TODO Auto-generated method stub
		String sql = "select * from \"Registration DB\".\"Courses\" where \"Registration DB\".\"Courses\".\"deptId\"=? and \"Registration DB\".\"Courses\".\"degree\"=? and \"Registration DB\".\"Courses\".\"isActive\"=true\r\n"
				+ "and \"Registration DB\".\"Courses\".\"courseId\" NOT IN(select \"Registration DB\".\"Enrollments\".\"courseId\" from \"Registration DB\".\"Enrollments\"\r\n"
				+ "where \"Registration DB\".\"Enrollments\".\"id\"=?)";
		Object[] args = { selectCriteria.getDeptId(), selectCriteria.getDegree(), selectCriteria.getUserId() };
		return jdbcTemplate.query(sql, args, new BeanPropertyRowMapper<Course>(Course.class));
	}

	// Fetch enrolled courses of student
	@Override
	public List<Course> fetchNotEnrolledCourses(User user) {
		// TODO Auto-generated method stub
		String sql = "select * from \"Registration DB\".\"Courses\" where \"Registration DB\".\"Courses\".\"isActive\"=true\r\n"
				+ "and \"Registration DB\".\"Courses\".\"courseId\" NOT IN(select \"Registration DB\".\"Enrollments\".\"courseId\" from \"Registration DB\".\"Enrollments\"\r\n"
				+ "where \"Registration DB\".\"Enrollments\".\"id\"=?)";
		Object[] args = { user.getId() };
		return jdbcTemplate.query(sql, args, new BeanPropertyRowMapper<Course>(Course.class));
	}

	// Add course
	@Override
	public int addCourse(Course course, String startDate, String endDate) {
		// TODO Auto-generated method stub
		String sql = "INSERT INTO \"Registration DB\".\"Courses\" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] args = { course.getCourseId(), course.getCourseName(), course.getDeptId(), this.getCurrentSemester(),
				Calendar.getInstance().get(Calendar.YEAR), true, course.getIsMandatory(),course.getStrength(),course.getDegree(),course.getProfessor(),
				course.getCourseMaxStrength(),course.getCourseTimings(),course.getDays(),startDate,endDate, course.getAmount()};
		int[] argTypes = { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, 
				Types.NUMERIC, Types.BOOLEAN, Types.BOOLEAN,Types.NUMERIC,Types.VARCHAR,Types.VARCHAR,
				 Types.NUMERIC, Types.VARCHAR, Types.VARCHAR,Types.VARCHAR, Types.VARCHAR, Types.NUMERIC};
		return jdbcTemplate.update(sql, args, argTypes);
		//comment;
	}

	// fetch Existing courses
	@Override
	public List<Course> fetchExistingCourses() {
		// TODO Auto-generated method stub
		String sql = "SELECT * FROM \"Registration DB\".\"Courses\"";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<Course>(Course.class));

	}

	//Delete course
	@Override
	public int deleteCourse(String courseId) {
		// TODO Auto-generated method stub
		String sql = "delete from \"Registration DB\".\"Courses\" where \"courseId\"=?";
		Object[] args = { courseId };
		int[] argTypes = { Types.VARCHAR };
		return jdbcTemplate.update(sql, args, argTypes);

	}
}
