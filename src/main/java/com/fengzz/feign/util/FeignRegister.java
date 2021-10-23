package com.fengzz.feign.util;


import com.fengzz.feign.annotation.FeginClinet;
import com.fengzz.feign.annotation.FeignGet;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * @author fengzz
 */
public class FeignRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanClassLoaderAware ,
                                        ResourceLoaderAware, BeanFactoryAware {
    //扫描--扫描到我们的自定义注解的bean，获取到bean上面的注解对象，并获取到相应的属性。
    //为每一个bean生成一个动态代理，通过动态代理去发起请求
    //希望我们能够把相应的bean放入到spring容器中

   private Environment environment;

   private ClassLoader classLoader;

   private ResourceLoader resourceLoader;

   private BeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        // 只有扫描到我们的自定义注解之后，我们才能去操作
        // 扫描到了之后需要有一个classloader这样才能去加载到我们容器内
        try {
            registerHttpRequest(beanDefinitionRegistry);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 注册动态bean
     */
    private void registerHttpRequest(BeanDefinitionRegistry beanDefinitionRegistry) throws ClassNotFoundException {
        //扫描我们的类，然后加载
        ClassPathScanningCandidateComponentProvider classScanner =getScanner();
        classScanner.setResourceLoader(this.resourceLoader);
        //指定标注了自定义注解的接口
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(FeginClinet.class);
        classScanner.addIncludeFilter(annotationTypeFilter);

        //扫描包
        String basePack = "com.fengzz";
        Set<BeanDefinition> beanDefinitionSet = classScanner.findCandidateComponents(basePack);
        for (BeanDefinition beanDefinition :beanDefinitionSet){
            if (beanDefinition instanceof  AnnotatedBeanDefinition){
               String className = beanDefinition.getBeanClassName();
                AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition)beanDefinition;
               //创建动态代理，并将我们的带注解的接口注册
                ((DefaultListableBeanFactory)this.beanFactory).registerSingleton(className,createProxy(annotatedBeanDefinition));
            }
        }
    }


    /**
     * 创建动态代理
     */
    private Object createProxy(AnnotatedBeanDefinition annotatedBeanDefinition) throws ClassNotFoundException {
        AnnotationMetadata annotationMetadata = annotatedBeanDefinition.getMetadata();
        Class<?> target = Class.forName(annotationMetadata.getClassName());
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            //拿到我们的注解
            FeginClinet annotation = target.getAnnotation(FeginClinet.class);
            String baseUrl  =annotation.baseUrl();
            if(method.getAnnotation(FeignGet.class)!=null) {
               FeignGet feginGet = method.getAnnotation(FeignGet.class);
               String url = baseUrl+feginGet.url();
                System.out.println("发起请求");
                String result = new RestTemplate().getForObject(url, String.class,"");
                return result;
            }
            throw new IllegalAccessException("不符合要求");
        };

        Object proxy = Proxy.newProxyInstance(this.classLoader, new Class[]{target}, invocationHandler);
        return proxy;
    }

    private ClassPathScanningCandidateComponentProvider getScanner(){

       return  new ClassPathScanningCandidateComponentProvider(false,this.environment){

            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                if(beanDefinition.getMetadata().isInterface()){
                    try {
                        Class<?> target = ClassUtils.forName(beanDefinition.getMetadata().getClassName(),classLoader);
                        return !target.isAnnotation();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return super.isCandidateComponent(beanDefinition);
            }
        };

    }


    @Override
    public void setEnvironment(Environment environment) {

            this.environment = environment;

    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {

           this.classLoader = classLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
           this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
           this.beanFactory = beanFactory;
    }
}
