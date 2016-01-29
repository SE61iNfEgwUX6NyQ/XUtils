public class BaseDao<T> {

	Connection conn;
	PreparedStatement st;
	ResultSet rs;
	
	JdbcUtil jdbcUtil = new JdbcUtil();
	
	int result = 0;
	
	private Class<T> persistentClass;
	
	@SuppressWarnings("unchecked")
	public BaseDaoUtil(){		
		conn = jdbcUtil.getConnection();
		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		persistentClass = (Class<T>) type.getActualTypeArguments()[0];		
	}
	
	
    /**
     * 保存
       * @param entity
      * @return
      */
	public int save(T entity) throws Exception{
		
		String sql = "INSERT INTO "+ entity.getClass().getSimpleName().toLowerCase() +" (";
		
		List<Method> list = this.matchPojoMethods(entity,"get");
		
		Iterator<Method> iter = list.iterator();
			
		Object obj[] = new Object[list.size()];
		int i = 0;
		//拼接字段顺序 insert into table name(id,name,email,
                  while(iter.hasNext()) {
                        Method method = iter.next();
                        sql += method.getName().substring(3).toLowerCase() + ","; 
                        if (method.getReturnType().getSimpleName().indexOf("Date") !=-1) {
				SimpleDateFormat sbf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				obj[i] = sbf.format(method.invoke(entity, new Object[]{}));
		      }else {
				obj[i] = method.invoke(entity, new Object[]{});
		      }          
            i++;
        }
        
        //去掉最后一个,符号insert insert into table name(id,name,email) values(
        sql = sql.substring(0, sql.lastIndexOf(",")) + ") values(";
        
        //拼装预编译SQL语句insert insert into table name(id,name,email) values(?,?,?,
        for(int j = 0; j < list.size(); j++) {
        	sql += "?,"; 
        }

        //去掉SQL语句最后一个,符号insert insert into table name(id,name,email) values(?,?,?);
        sql = sql.substring(0, sql.lastIndexOf(",")) + ")";
        
        //到此SQL语句拼接完成,打印SQL语句
        System.out.println(sql);
        
        try {
        	st = conn.prepareStatement(sql);
        	for (int j = 0; j < obj.length; j++) {
				st.setObject(j+1, obj[j]);
			}
			result = st.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
		jdbcUtil.getClose(rs, st, conn);
		
		return result;	
	}
	
	/**
	 * 删除
	 * @param object
	 * @return
	 * @throws SQLException
	 */
	public int deleteId(Object object) throws Exception{
		
		String sql = "delete from "+ persistentClass.getSimpleName().toLowerCase() +" where ";
		
		//通过子类的构造函数,获得参数化类型的具体类型.比如BaseDAO<T>也就是获得T的具体类型
        T entity = persistentClass.newInstance();
        
        //存放Pojo(或被操作表)主键的方法对象
        Method idMethod = null;
        
        List<Method> list = this.matchPojoMethods(entity, "set");
        Iterator<Method> iter = list.iterator();
        
        //过滤取得Method对象
        while(iter.hasNext()) {
            Method tempMethod = iter.next();
            if(tempMethod.getName().indexOf("Id") != -1 && tempMethod.getName().substring(3).length() == 2) {
                idMethod = tempMethod;
            } else if((entity.getClass().getSimpleName() + "Id").equalsIgnoreCase(tempMethod.getName().substring(3))){
                idMethod = tempMethod;
            }
        }
        //第一个字母转为小写
        sql += idMethod.getName().substring(3,4).toLowerCase()+idMethod.getName().substring(4) + " = ?";
       	
		System.out.println(sql);
		
		st = conn.prepareStatement(sql);
		
		//判断id的类型
        if(object instanceof Integer) {
            st.setInt(1, (Integer)object);
        } else if(object instanceof String){
            st.setString(1, (String)object);
        }
		
		result = st.executeUpdate();
		
		jdbcUtil.getClose(rs, st, conn);
		
		return result;	
	}
	
	/**
	 * 修改
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public int update(T entity) throws Exception{
		
		String sql = "update "+ entity.getClass().getSimpleName().toLowerCase() +" set ";
		
		List<Method> list = this.matchPojoMethods(entity, "get");
		
		//装载参数
		Object obj[] = new Object[list.size()];
		int i = 0;
		
		//临时Method对象,负责迭代时装method对象.
        Method tempMethod = null;
        
        //由于修改时不需要修改ID,所以按顺序加参数则应该把Id移到最后.
        Method idMethod = null;
        Iterator<Method> iter = list.iterator();
        while(iter.hasNext()) {
            tempMethod = iter.next();
 
            //如果方法名中带有ID字符串并且长度为2,则视为ID.
            if(tempMethod.getName().lastIndexOf("Id") != -1 && tempMethod.getName().substring(3).length() == 2) {
            	obj[list.size()-1] = tempMethod.invoke(entity, new Object[]{});
                //把ID字段的对象存放到一个变量中,然后在集合中删掉.
                idMethod = tempMethod;
                iter.remove();
            //如果方法名去掉set/get字符串以后与pojo + "id"想符合(大小写不敏感),则视为ID
            } else if((entity.getClass().getSimpleName() + "Id").equalsIgnoreCase(tempMethod.getName().substring(3))) {
            	obj[list.size()-1] = tempMethod.invoke(entity, new Object[]{});
            	idMethod = tempMethod;
                iter.remove();                
            }
        }
		
        //把迭代指针移到第一位
        iter = list.iterator();
        while(iter.hasNext()) {
            tempMethod = iter.next();
            sql += tempMethod.getName().substring(3).toLowerCase() + "= ?,";
            obj[i] = tempMethod.invoke(entity, new Object[]{});
            i++;
        }
        
        //去掉最后一个,符号
        sql = sql.substring(0,sql.lastIndexOf(","));
        
        //添加条件
        sql += " where " + idMethod.getName().substring(3).toLowerCase() + " = ?";
        
        //SQL拼接完成,打印SQL语句
        System.out.println(sql);
		
        st = conn.prepareStatement(sql);
        
        for (int j = 0; j < obj.length; j++) {
			st.setObject(j+1, obj[j]);
		}
        
        result = st.executeUpdate();

        jdbcUtil.getClose(rs, st, conn);
        
		return result;

	}
	
	
	public T findById(Object object) throws Exception{
		
		String sql = "select * from "+ persistentClass.getSimpleName().toLowerCase() +" where ";
		
		//通过子类的构造函数,获得参数化类型的具体类型.比如BaseDAO<T>也就是获得T的具体类型
        T entity = persistentClass.newInstance();
        
        //存放Pojo(或被操作表)主键的方法对象
        Method idMethod = null;
        
        List<Method> list = this.matchPojoMethods(entity, "set");
        Iterator<Method> iter = list.iterator();
        
        //过滤取得Method对象
        while(iter.hasNext()) {
            Method tempMethod = iter.next();
            if(tempMethod.getName().indexOf("Id") != -1 && tempMethod.getName().substring(3).length() == 2) {
                idMethod = tempMethod;
            } else if((entity.getClass().getSimpleName() + "Id").equalsIgnoreCase(tempMethod.getName().substring(3))){
                idMethod = tempMethod;
            }
        }
        //第一个字母转为小写
        sql += idMethod.getName().substring(3,4).toLowerCase()+idMethod.getName().substring(4) + " = ?";
       	
		System.out.println(sql);
		
		st = conn.prepareStatement(sql);
		
		//判断id的类型
        if(object instanceof Integer) {
            st.setInt(1, (Integer)object);
        } else if(object instanceof String){
            st.setString(1, (String)object);
        }
		
        rs = st.executeQuery();
                
        //把指针指向迭代器第一行
        iter = list.iterator();
        
        //封装
        while(rs.next()) {
            while(iter.hasNext()) {
                Method method = iter.next();
                if(method.getParameterTypes()[0].getSimpleName().indexOf("String") != -1) {
                    //由于list集合中,method对象取出的方法顺序与数据库字段顺序不一致(比如:list的第一个方法是setDate,而数据库按顺序取的是"123"值)
                    //所以数据库字段采用名字对应的方式取.
                    this.setString(method, entity, rs.getString(method.getName().substring(3).toLowerCase()));
                } else if(method.getParameterTypes()[0].getSimpleName().indexOf("Date") != -1){
                    this.setDate(method, entity, rs.getDate(method.getName().substring(3).toLowerCase()));
                }else {
                    this.setInt(method, entity, rs.getInt(method.getName().substring(3).toLowerCase()));
                }    
            }
        }
		
        jdbcUtil.getClose(rs, st, conn);
        
		return entity;
		
	}
	
	
	
	
	
	/**
     * 过滤当前Pojo类所有带传入字符串的Method对象,返回List集合.
     */
    private List<Method> matchPojoMethods(T entity,String methodName) {
        //获得当前Pojo所有方法对象
        Method[] methods = entity.getClass().getDeclaredMethods();
        
        //List容器存放所有带get字符串的Method对象
        List<Method> list = new ArrayList<Method>();
        
        //过滤当前Pojo类所有带get字符串的Method对象,存入List容器
        for(int index = 0; index < methods.length; index++) {
            if(methods[index].getName().indexOf(methodName) != -1) {
                list.add(methods[index]);
            }
        }        
        return list;
    }
    

    /**
     * 参数类型为String时,为entity字段设置参数,对应set
     */
    public String setString(Method method, T entity, String arg) throws Exception{
        return (String)method.invoke(entity, new Object[]{arg});
    }
    
   
    /**
     * 参数类型为Date时,为entity字段设置参数,对应set
     */
    public Date setDate(Method method, T entity, Date arg) throws Exception{
        return (Date)method.invoke(entity, new Object[]{arg});
    }
	
	
    /**
     * 参数类型为Integer或int时,为entity字段设置参数,对应set
     */
    public Integer setInt(Method method, T entity, Integer arg) throws Exception{
        return (Integer)method.invoke(entity, new Object[]{arg});
    }
    
	
	
	
}