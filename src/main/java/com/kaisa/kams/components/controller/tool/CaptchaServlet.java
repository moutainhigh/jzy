/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kaisa.kams.components.controller.tool;

import com.kaisa.kams.components.security.ShiroSession;

import nl.captcha.Captcha;
import nl.captcha.noise.CurvedLineNoiseProducer;
import nl.captcha.noise.NoiseProducer;
import nl.captcha.servlet.CaptchaServletUtil;
import nl.captcha.servlet.SimpleCaptchaServlet;
import nl.captcha.text.producer.DefaultTextProducer;
import nl.captcha.text.renderer.DefaultWordRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义验证码样式
 *
 * @author tank
 */
public class CaptchaServlet extends SimpleCaptchaServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Font> textFonts = Arrays.asList(new Font("Courier", Font.PLAIN, 35));
        Color imageCaptchaColor = new Color(255, 102, 0);
        Color noise1Color = new Color(0.48235294f, 0.8f, 0.45882353f);
        Color noise2Color = new Color(0.8f, 0.36862746f, 0.40392157f);
        Color noise3Color = new Color(0.8f, 0.7372549f, 0.44313726f);
        Color noise4Color = new Color(0.22745098f, 0.21176471f, 0.27058825f);
        NoiseProducer noise1Producer = new CurvedLineNoiseProducer(noise1Color, 0.8f);
        NoiseProducer noise2Producer = new CurvedLineNoiseProducer(noise2Color, 0.8f);
        NoiseProducer noise3Producer = new CurvedLineNoiseProducer(noise3Color, 0.8f);
        NoiseProducer noise4Producer = new CurvedLineNoiseProducer(noise4Color, 0.9999f);
        Captcha captcha = new Captcha.Builder(120, 40)
                .addText(new DefaultTextProducer(), new DefaultWordRenderer(imageCaptchaColor, textFonts))
                .addNoise(noise1Producer)
                .addNoise(noise2Producer)
                .addNoise(noise3Producer)
                .addNoise(noise4Producer)
                .build();
        ShiroSession.setAttribute(Captcha.NAME,captcha);
        CaptchaServletUtil.writeImage(response, captcha.getImage());
    }
}