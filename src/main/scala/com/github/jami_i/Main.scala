package com.github.jami_i

import java.io.File
import java.net.URL

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc.CvFont
import org.bytedeco.javacpp.opencv_objdetect._
import org.bytedeco.javacpp.{ Loader, opencv_imgproc, opencv_objdetect}
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat
import org.bytedeco.javacv.{CanvasFrame, OpenCVFrameGrabber}

object Main {

  def main(args: Array[String]) {
    val grabber = new OpenCVFrameGrabber(0)
    grabber.start()
    val initialImage = grabber.grab()

    val canvasFrame = new CanvasFrame("Cam")
    canvasFrame.setCanvasSize(initialImage.imageWidth / 2, initialImage.imageHeight / 2)

    grabber.setFrameRate(grabber.getFrameRate)

    Loader.load(classOf[opencv_objdetect])

    val faceCascade = new CascadeClassifier(classifier(args).getAbsolutePath)

    while (canvasFrame.isVisible) {

      val converter = new ToMat()
      val color = converter.convert(grabber.grab())

      val small = new Mat()

      opencv_imgproc.resize(color, small, new Size(color.size().width() / 2, color.size().height() / 2))

      val greyMat = new Mat()
      opencv_imgproc.cvtColor(small, greyMat, opencv_imgproc.CV_BGR2GRAY, 1)

      val equalizedMat = new Mat()
      opencv_imgproc.equalizeHist(greyMat, equalizedMat)

      val faceRects = new RectVector()

      faceCascade.detectMultiScale(equalizedMat, faceRects)

      val font = new CvFont()
      opencv_imgproc.cvInitFont(font, opencv_imgproc.CV_FONT_HERSHEY_SIMPLEX, 0.5, 0.5)

      toSeq(faceRects)
      .filterNot(r => {
        r != null && (r.x() < 0 || r.y() < 0) && (r.x() >= small.size().width() || r.y() >= small.size().height())
      }).zipWithIndex.foreach(tuple => {
        val r = tuple._1
        val num = "%03d".format(tuple._2)
        if(r != null){
          System.out.print(s"x : ${r.x()}, y : ${r.y()}\r")
          opencv_imgproc.rectangle(small, r, new Scalar(0, 255,0, 0))
          opencv_imgproc.putText(small, num, new Point(r.x(), r.y()), 1, 1, new Scalar(0, 255, 0, 0))
        }
      })

      canvasFrame.showImage(converter.convert(small))
    }
    grabber.stop()
    canvasFrame.dispose()
  }

  def classifier(args:Array[String]):File = {
    import scalaz._
    import Scalaz._
    args.toSeq.headOption
      .map(f => new File(f))
      .getOrElse{
        val url = new URL("https://raw.github.com/Itseez/opencv/2.4.0/data/haarcascades/haarcascade_frontalface_alt.xml")
        println("load external resource : " + url)
        Loader.extractResource(url, null, "classifier", ".xml")  <| {_.deleteOnExit()}
      }
  }

  def toSeq(rects:RectVector):Seq[Rect] = {
    (0L to rects.size()).map( i => rects.get(i))
  }

}
