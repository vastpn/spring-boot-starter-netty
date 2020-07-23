//package com.centify.boot.web.embedded.netty;
//
//import io.netty.util.Recycler;
//import lombok.extern.log4j.Log4j2;
//
//import java.util.UUID;
//
///**
// * <pre>
// * <b>TODO</b>
// * <b>Describe:TODO</b>
// *
// * <b>Author: tanlin [2020/7/14 15:04]</b>
// * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
// * <b>Changelog:</b>
// *   Ver   Date                  Author           Detail
// *   ----------------------------------------------------------------------------
// *   1.0   2020/7/14 15:04        tanlin            new file.
// * <pre>
// */
//@Log4j2
//public class RecyclerDemo {
//    private static final Recycler<User> RECYCLER = new Recycler<User>() {
//        @Override
//        protected User newObject(Handle<User> handle) {
//            return new User(handle);
//        }
//    };
//    public static void main(String[] args){
//
//        for (int i=0;i<1000000;i++){
//            new Thread(()->{
//                User user = RECYCLER.get();
//                user.setItemd(UUID.randomUUID().toString());
//                log.info(user.getItemd());
//                try {
//                    Thread.sleep(200L);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                user.recycle();
//            }).start();
//        }
//
//
//    }
//}
