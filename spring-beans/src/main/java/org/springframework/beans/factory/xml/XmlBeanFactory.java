/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.xml;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;

/**
 * Convenience extension of {@link DefaultListableBeanFactory} that reads bean definitions
 * from an XML document. Delegates to {@link XmlBeanDefinitionReader} underneath; effectively
 * equivalent to using an XmlBeanDefinitionReader with a DefaultListableBeanFactory.
 *
 * <p>The structure, element and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). "beans" doesn't need to be the root element of the XML
 * document: This class will parse all bean definition elements in the XML file.
 *
 * <p>This class registers each bean definition with the {@link DefaultListableBeanFactory}
 * superclass, and relies on the latter's implementation of the {@link BeanFactory} interface.
 * It supports singletons, prototypes, and references to either of these kinds of bean.
 * See {@code "spring-beans-3.x.xsd"} (or historically, {@code "spring-beans-2.0.dtd"}) for
 * details on options and configuration style.
 *
 * <p><b>For advanced needs, consider using a {@link DefaultListableBeanFactory} with
 * an {@link XmlBeanDefinitionReader}.</b> The latter allows for reading from multiple XML
 * resources and is highly configurable in its actual XML parsing behavior.
 *
 * 1.封装资源文件。当进入XmlBeanDefinitionReader后首先对参数Resource使用EncodeResource类进行封装。
 * 2.获取输入流。从Resource中获取对应的InputStream并构造InputSource。
 * 3.通过构造的InputSource实例和Resource实例继续调用函数loadBeanDefinitions --> doLoadBeanDefinitions。
 *
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 15 April 2001
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see XmlBeanDefinitionReader
 * @deprecated as of Spring 3.1 in favor of {@link DefaultListableBeanFactory} and
 * {@link XmlBeanDefinitionReader}
 */
@Deprecated
@SuppressWarnings({"serial", "all"})
public class XmlBeanFactory extends DefaultListableBeanFactory {

	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);


	/**
	 * Create a new XmlBeanFactory with the given resource,
	 * which must be parsable using DOM.
	 * @param resource the XML resource to load bean definitions from
	 * @throws BeansException in case of loading or parsing errors
	 */
	public XmlBeanFactory(Resource resource) throws BeansException {
		this(resource, null);
	}

	/**
	 * Create a new XmlBeanFactory with the given input stream,
	 * which must be parsable using DOM.
	 * @param resource the XML resource to load bean definitions from
	 * @param parentBeanFactory parent bean factory
	 * @throws BeansException in case of loading or parsing errors
	 *
	 * 在XmlBeanFactory构造函数中 完成XML 解析和加载 注册
	 *
	 * 在getBean 加载bean
	 *
	 * 		BeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("spring/spring-test.xml"));
	 * 		MySpringBean bean = (MySpringBean) beanFactory.getBean("mySpringBean");
	 *
	 *		(一) BeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("spring/spring-test.xml"));
	 *
	 *		XmlBeanFactory  持有 XmlBeanDefinitionReader reader 调用 loadBeanDefinitions(Resource resource)
	 *		--> loadBeanDefinitions(EncodedResource encodedResource)
	 *		--> doLoadBeanDefinitions(InputSource inputSource, Resource resource)(真正的核心逻辑入口)
	 *		-->(
	 *				1.把 xlm文件编程 Document doc = doLoadDocument(inputSource, resource);
	 *				2.根据配置文件 注册bean int count = registerBeanDefinitions(doc, resource);
	 *			)
	 *		1.把 xlm文件编程 Document doc = doLoadDocument(inputSource, resource); --> 很简单就是普通的sax解析
	 *		2.根据配置文件 注册bean 返回最新注册的数量 int count = registerBeanDefinitions(doc, resource); -->
	 *			(
	 *				2.1 BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader(); 创建XmlBeanDefinitionReader的委派类 BeanDefinitionDocumentReader
	 *					调用委派类实现逻辑 传入文档对象和 解析过程中需要的资源(配置文件：resource,NamespaceHandlerResolver 等) documentReader.registerBeanDefinitions(Document doc, XmlReaderContext readerContext)-->
	 *				(
	 *					2.1.1 doRegisterBeanDefinitions(Element root)-->
	 *						(
	 *							2.1.1.1 创建委托对象  委托实现RegisterBeanDefinitions
	 *									BeanDefinitionParserDelegate delegate = createDelegate( XmlReaderContext readerContext, Element root, BeanDefinitionParserDelegate parentDelegate)
	 *							2.1.1.2	解析前处理，空实现留给子类实现 preProcessXml(root);
	 *							2.1.1.3	parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate);-->
	 *									(
	 *										2.1.1.3.1 默认的（spring 官方的）命名空间 delegate.isDefaultNamespace(root) == true -->
	 *										(
	 * 											2.1.1.3.1.1 官方命名空间下的标准标签 parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) -->
	 * 											(
	 *												2.1.1.3.1.1 对import标签的处理  importBeanDefinitionResource(Element ele);
	 *												2.1.1.3.1.2 对alias标签的处理 processAliasRegistration(Element ele);
	 *												2.1.1.3.1.3 对bean标签的处理  processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate); -->
	 *															(
	 *																2.1.1.3.1.3.1 返回一个返回一个BeanDefinitionHolder持有GenericBeanDefinition 包含class、name、id、alias之类的属性
	 *																				BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);-->
	 *																				(
	 *																					parseBeanDefinitionElement(Element ele, BeanDefinition containingBean)-->
	 *																					(
	 *																						parseBeanDefinitionElement(Element ele, String beanName, @Nullable BeanDefinition containingBean)-->
	 *																						(
	 *																							createBeanDefinition(@Nullable String className, @Nullable String parentName)-->
	 *																							(
	 *																								创建用于承载属性的AbstractBeanDefinition类型的GenericBeanDefinition
	 *																								AbstractBeanDefinition bd = createBeanDefinition(String parentName,String className,ClassLoader classLoader)
	 *																								硬编码解析默认bean的各种属性
	 *																								parseBeanDefinitionAttributes(Element ele, String beanName, BeanDefinition containingBean, AbstractBeanDefinition bd)
	 *																							)
	 *																						)
	 *																					)
	 *
	 *																				)
	 *																2.1.1.3.1.3.2 寻找自定义标签并根据自定义标签寻找命名空间处理器，并进行进一步的解析
	 *																				delegate.decorateBeanDefinitionIfRequired(Element ele, BeanDefinitionHolder originalDef)
	 *																2.1.1.3.1.3.3 BeanDefinitionReaderUtils.registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)-->
 *																				  (
	 *																				  2.1.1.3.1.3.3.1 使用beanName做唯一标识注册 String beanName = definitionHolder.getBeanName();
	 *																				  				  registry.registerBeanDefinition(String beanName, BeanDefinition beanDefinition)-->
	 *																				  				  (
	 *																				  				  	 2.1.1.3.1.3.3.1 注册前的最后一次校验，这里的校验不同于之前的XML文件校验，
	 *																				  				  					 主要是对于AbstractBeanDefinition属性中的methodOverrides校验，
	 *																				  				  					 校验methodOverrides是否与工厂方法并存或者methodOverrides对应的方法根本不存在
	 *																				  				  					((AbstractBeanDefinition) beanDefinition).validate();
	 *																				  				  	2.1.1.3.1.3.3.2  对beanName已经注册的情况的处理。如果设置了不允许bean的覆盖，则需要抛出异常，否则直接覆盖
	 *																				  				  					 BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
	 *																				  				  					 !isAllowBeanDefinitionOverriding() == true
*																				  				  		2.1.1.3.1.3.3.3  注册beanDefinition 加入map缓存
	 *																				  				  					 this.beanDefinitionMap.put(beanName, beanDefinition);
	 *																				  				  					 记录beanName
	 *																				  				  					 this.beanDefinitionNames.add(beanName);
	 *																				  				  	2.1.1.3.1.3.3.4	 重置所有beanName对应的缓存
	 *																				  				  					 resetBeanDefinition(String beanName)
	 *																				  				  )
	 *
	 *																				  2.1.1.3.1.3.3.2 注册所有的别名 String[] aliases = definitionHolder.getAliases();
	 *																				  				  registry.registerAlias(beanName, alias);-->
	 *																				  				  (
	 *																				  				  	2.1.1.3.1.3.3.2.1 如果beanName与alias相同的话，不记录alias，并删除对应的alias
	 *																				  				  						   alias.equals(name);this.aliasMap.remove(alias);
	 *																				  				  	2.1.1.3.1.3.3.2.2 如果alias不允许被覆盖则抛出异常
	 *																				  				  		 				   String registeredName = this.aliasMap.get(alias);
	 *																				  				  	2.1.1.3.1.3.3.2.3 当A -> B存在时，若再次出现 A -> C -> B时候则会抛出异常
	 *																				  				  )
 *																				  )
	 *
	 *															)
	 *												2.1.1.3.1.4 对beans标签的处理 doRegisterBeanDefinitions(Element ele);
	 * 											)
	 *										)
	 *
	 *										2.1.1.3.2 非默认的（自定义的）命名空间 delegate.isDefaultNamespace(root) == tue -->
	 *									)
	 *							2.1.1.4 解析后处理，空实现 留给子类实现 postProcessXml(root);
	 *						)
	 *				)
	 *
	 *			)
	 *
	 *		(二) MySpringBean bean = (MySpringBean) beanFactory.getBean("mySpringBean");
	 *			1. 加载bean getBean(String name)--> doGetBean(final String name,  final Class<T> requiredType, final Object[] args, boolean typeCheckOnly)-->
	 *			    (
	 *			    	1.1 提取对应的beanName
	 *			    		final String beanName = transformedBeanName(name);
	 *			    	1.2 尝试从缓存中加载单例
	 *			    		Object sharedInstance = getSingleton(String beanName)--> getSingleton(String beanName, boolean allowEarlyReference)-->
	 *			    		(
	 *			    				1.2.1 singletonObject 是 用于保存 beanName 和创建 bean 实例之间的关系
	 *			    						Object singletonObject = this.singletonObjects.get(beanName);
	 *			    				1.2.3 isSingletonCurrentlyInCreation(String beanName) && singletonObject == null == true
	 *			    				      也是保存beanName 和创建bean实例之间的关系，与 singletonObjects的不同之处在于，
	 *			    						当一个单例bean被放到这里面后，那么当bean还在创建过程中，
	 *			    						就可以通过getBean方法获取到了，其目的是用来检測循环引用
	 *			    						singletonObject = this.earlySingletonObjects.get(beanName);
	 *								1.2.4 singletonObject == null && allowEarlyReference == true
	 *								1.2.5 ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
	 *								1.2.6 singletonFactory != null
	 *								1.2.7 singletonObject = singletonFactory.getObject();
	 *								1.2.8 this.earlySingletonObjects.put(beanName, singletonObject);
	 *									  this.singletonFactories.remove(beanName);
	 *								1.2.9 mbd == null 尝试从缓存中加栽bean
	 *									  object = FactoryBeanRegistrySupport.getCachedObjectForFactoryBean(String beanName)
	 *								1.2.10 object == null 到这里已经明确知道beanInstance—定是FactoryBean类型
	 *										1.2.10.1	getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) -->
 *										    			(
	 *														1.2.10.1.1 factory.isSingleton() && containsSingleton(beanName) == true
	 *														1.2.10.1.2 Object object = this.factoryBeanObjectCache.get(beanName)
	 *														1.2.10.1.3 object	== null
	 *																	object = doGetObjectFromFactoryBean(factory, beanName);-->
	 *																	(
	 *																		1.2.10.1.1.1 验证权限
	 *																		1.2.10.1.1.2 直接调用getObject方法
	 *																				 	 object = factory.getObject();
*																			1.2.10.1.1.3 isSingletonCurrentlyInCreation
	 *																					 return object;
	 *																	)
*															1.2.10.1.4 beforeSingletonCreation(String beanName); -->
 *																	   (
	 *																		记录加载状态，也就是通过this.singletonsCurrentlylnCreation.add(beanName)
	 *																		将当前正要创建的bean记录在缓存中，这样便可以对循环依赖进行检测
	 *																   )
	 *													    1.2.10.1.5 postProcessObjectFromFactoryBean(Object object, String beanName)--> applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)-->
	 *													    		  (
	 *																	遍历	List<BeanPostProcessor> beanPostProcessors 每一个 beanPostProcessor
	 *													    		  )
	 *													    1.2.10.1.6 afterSingletonCreation(String beanName) -->
	 *													    		 (
	 *													    		 	当bean加载结束后需要移除缓存中对该bean的正在加载状态的记录。
	 *													    		 )
	 *													    1.2.10.1.7 containsSingleton(String beanName) -->
	 *													    		   (
	 *													    		   		return this.singletonObjects.containsKey(beanName);
	 *													    		   )
	 *													    		   this.factoryBeanObjectCache.put(beanName, object);
	 *
	 *													    		   return object;
 *										   				 )
	 *			    		)
	 *			    	1.3 诸如BeanFactory的情况并不是直接返回实例本身而是返回指定方法返回的实例
	 *			    		用于检测当前bean是否是FactoryBean类型的bean，
	 *			    		如果是，那么需要调用该bean对应的FactoryBean实例中的getObject()作为返回值。
	 *			    		Object bean = getObjectForBeanInstance(Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd)-->
	 *			    		(
	 *			    			1.3.1 如果指定的name是工厂相关（以&为前缀）且 beanInstance又不是FactoryBean类型则验证不通过
 *			    						BeanFactoryUtils.isFactoryDereference(name) == true && beanInstance instanceof NullBean
	 *			    					return 	beanInstance
	 *			    					!(beanInstance instanceof FactoryBean)
	 *			    					报错
	 *			    			1.3.2 !(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)
	 *									return beanInstance;
	 *			    			1.3.3
	 *			    		)
	 *			    	1.4 只有在单例情况才会尝试解决循环依赖，报出异常，原型模式情况下，如果存在A中有B的属性，B中有A的属性，
	 *			    		那么当依赖注入的时候，就会产生当A还未创建完的时候因为对于B的创建再次返回创建A，造成循环依赖
	 *			    		isPrototypeCurrentlyInCreation(String beanName)
	 *			    	1.5 如果beanDefinitionMap中也就是在所有已经加载的类中不包括beanName则尝试从parentBeanFactory中检测
	 *			    		BeanFactory parentBeanFactory = getParentBeanFactory();
	 *			    		((AbstractBeanFactory) parentBeanFactory).doGetBean(nameToLookup, requiredType, args, typeCheckOnly);
	 *			    	1.6 将存储XML配置文件的GenericBeanDefinition转换为RootBeanDefinition
	 *			    		如果指定beanName是子Bean的话同时会合并父类的相关属性
	 *			    		final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
	 *
	 *			    	1.7 若存在依赖则需要递归实例化依赖bean
 *			    			String[] dependsOn = mbd.getDependsOn();
	 *			    		for (String dep : dependsOn)
	 *			    		registerDependentBean(dep, beanName);
	 *			    		getBean(dep);
	 *
	 *			    	1.8 mbd.isSingleton()
	 *			    		bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
	 *			    		sharedInstance = getSingleton(beanName, () -> { createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)}) -->
	 *			    		(
	 *			    			// 首先检査对应的bean是否已经加载过，因为singleton模式其实就是复用已创建的bean，所以这一步是必须的
	 * 							Object singletonObject = this.singletonObjects.get(beanName);
	 * 							// 如果为空才可以进行singleton的bean的初始化
	 * 							if (singletonObject == null)
	 * 							{
	 * 							    // 记录加载状态，也就是通过this.singletonsCurrentlylnCreation.add(beanName)
	 * 							    // 将当前正要创建的bean记录在缓存中，这样便可以对循环依赖进行检测。
	 * 								beforeSingletonCreation(beanName);
	 * 								// 初始化bean
	 * 								singletonObject = singletonFactory.getObject();
	 * 								newSingleton = true;
	 * 								afterSingletonCreation(beanName);
	 * 								if (newSingleton)
	 * 								{
	 * 									// 加入缓存 并删除加载bean过程中所记录的各种辅助状态。
	 * 									addSingleton(beanName, singletonObject);
 * 									}
	 * 							}
	 * 								return singletonObject;
	 *			    		)
	 *			    		createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)}) -->
	 *			    		(
	 *			    			RootBeanDefinition mbdToUse = mbd;
	 *							// 锁定class，根据设置的class属性或者根据className来解析Class
	 * 							Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
	 * 							// 验证及准备覆盖的方法
	 * 							mbdToUse.prepareMethodOverrides();-->
	 * 							(
	 * 								Spring中确实没有override-method这样的配置，但是如果读过前面的部分，可能会有所发现，
	 * 								在Spring配置中是存在lookup~method和replace-method的，
	 * 								而这两个配置的加载其实就是将配置统一存放在BeanDefinition中的methodOverrides属性里，
	 * 								而这个函数的操作其实也就是针对于这两个配置的。
	 * 							)
	 * 							// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
	 * 							// 给BeanPostProcessors一个机会返回代理来替代真正的实例
	 * 							Object bean = resolveBeforeInstantiation(beanName, mbdToUse);-->
	 * 							(
	 * 								bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
	 *
	 * 								bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
	 *
	 * 								return bean;
	 * 							)
	 * 							Object beanInstance = 	doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args) -->
	 * 							(
	 *
	 * 							)
	 *			    		)
	 *						mbd.isPrototype()
	 *						bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
	 *						else
	 *						根据scopeName来创建
	 *
	 *			    )
	 *
	 *
	 *
	 *
	 * 		Resource ： spring用来抽象各种资源 例如： 配置文件
	 *
	 */
	public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		// XmlBeanDefinitionReader去加载加载BeanDefinitions
		this.reader.loadBeanDefinitions(resource);
	}

}
