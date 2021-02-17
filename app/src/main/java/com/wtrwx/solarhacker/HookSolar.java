package com.wtrwx.solarhacker;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.io.File;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class HookSolar implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    static XSharedPreferences pluginPreferences;
    Context context = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Hook isXposedActive()
        if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            Class<?> classAppUtils = XposedHelpers.findClassIfExists(BuildConfig.APPLICATION_ID + ".utils.AppInfo", lpparam.classLoader);
            if (classAppUtils != null) {
                XposedHelpers.findAndHookMethod(classAppUtils, "isXposedActive", XC_MethodReplacement.returnConstant(true));
                XposedBridge.log("[SolarHacker]Hook isXposedActive() Success");
            }
        }

        String appName = "com.fenbi.android.solar";
        String packageName = "com.stub";
        String className = "StubApp";
        String methodName = "getOrigApplicationContext";
        if (pluginPreferences.getBoolean("switch_hook", true) && lpparam.packageName.equals(appName)) {
            XposedBridge.log("[SolarHacker]get classloader start\n");
            Class clazz = lpparam.classLoader.loadClass(packageName + "." + className);
            try {
                findAndHookMethod(clazz, methodName, Context.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        // Get 360 Context
                        context = (Context) param.args[0];
                        // Get realClassLoader and hook with this ClassLoader
                        ClassLoader realClassLoader = context.getClassLoader();
                        pluginPreferences.reload();
                        if (pluginPreferences.getBoolean("rm_splash_ad", true)) {
                            removeSplashAD(realClassLoader);
                        }
                        if (pluginPreferences.getBoolean("rm_mainactivity_ad", true)) {
                            removeMainActivityAD(realClassLoader);
                        }
                        if (pluginPreferences.getBoolean("crack_videovip_verification", true)) {
                            hookVideoVip(realClassLoader);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            XposedBridge.log("[SolarHacker]get classloader end \n");
        }
    }

    private void hookVideoVip(ClassLoader realClassLoader) throws ClassNotFoundException {
        Class clasz1 = realClassLoader.loadClass("com.fenbi.android.solar.data.VipUserVO");
        findAndHookMethod(clasz1, "isVip", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                        //XposedBridge.log("[SolarHacker]Set isVip true");
                    }
                }
        );
        Class clasz2 = realClassLoader.loadClass("com.fenbi.android.solar.data.QuestionInfo");
        findAndHookMethod(clasz2, "isHasVip", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                        //XposedBridge.log("[SolarHacker]Set isHasVip true");
                    }
                }
        );
        Class clasz3 = realClassLoader.loadClass("com.fenbi.android.solar.data.auth.AuthResultVO");
        findAndHookMethod(clasz3, "isAuthorised", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                        //XposedBridge.log("[SolarHacker]Set isAuthorised true");
                    }
                }
        );
        Class clasz4 = realClassLoader.loadClass("com.fenbi.android.solar.data.auth.VipVideoReplayVO");
        findAndHookMethod(clasz4, "getViewTimeLimit", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(999999999);
                        //XposedBridge.log("[SolarHacker]Set getViewTimeLimit 999999999");
                    }
                }
        );
        //XposedBridge.log("[SolarHacker]HookVideoVip Success");
    }

    private void removeSplashAD(final ClassLoader realClassLoader) throws ClassNotFoundException {
        Class cls = realClassLoader.loadClass("com.fenbi.android.solar.activity.RouterActivity");
        findAndHookMethod(cls, "startActivity", Intent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = (Intent) param.args[0];
                        if (String.valueOf(intent).contains("SplashActivity")) {
                            Class cls = XposedHelpers.findClass("com.fenbi.android.solar.activity.HomeActivity", realClassLoader);
                            Intent newIntent = new Intent(context, cls);
                            param.args[0] = newIntent;
                            //XposedBridge.log("[SolarHacker]RemoveSplashAD Success");
                        }
                    }
                }
        );
    }

    private void removeMainActivityAD(final ClassLoader realClassLoader) throws ClassNotFoundException {
        Class cls = realClassLoader.loadClass("com.fenbi.android.solar.firework.MainFragmentDynamicAd");
        findAndHookMethod(cls, "a", List.class, new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return null;
                    }
                }
        );
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File prefsFileProt =
                    new File("/data/user_de/0/"+BuildConfig.APPLICATION_ID+"/shared_prefs/"+BuildConfig.APPLICATION_ID+"_preferences.xml");
            pluginPreferences = new XSharedPreferences(prefsFileProt);
        } else {
            pluginPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        }
    }
}
