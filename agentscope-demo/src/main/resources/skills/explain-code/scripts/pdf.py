#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
PDF 生成工具
使用方法: python pdf.py "pdf内容"
"""

import sys
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

def create_pdf(text_content, output_filename="output.pdf"):
    """
    创建 PDF 文件

    Args:
        text_content (str): 要写入 PDF 的文本内容
        output_filename (str): 输出文件名
    """
    c = canvas.Canvas(output_filename, pagesize=A4)
    width, height = A4

    # 设置中文字体
    try:
        pdfmetrics.registerFont(TTFont('SimSun', 'simsun.ttc'))
        c.setFont("SimSun", 12)
    except:
        c.setFont("Helvetica", 12)  # fallback 字体

    # 处理文本换行
    lines = text_content.split('\n')

    y_position = height - 40  # 从页面顶部开始
    line_height = 15

    for line in lines:
        if y_position < 40:  # 如果当前页空间不足，创建新页
            c.showPage()
            y_position = height - 40

        c.drawString(40, y_position, line)
        y_position -= line_height

    c.save()
    print(f"PDF 文件已生成: {output_filename}")

def main():
    if len(sys.argv) < 2:
        print("使用方法: python pdf.py \"pdf内容\"")
        sys.exit(1)

    text_content = sys.argv[1]
    create_pdf(text_content)

if __name__ == "__main__":
    main()
