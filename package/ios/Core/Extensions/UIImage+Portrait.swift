//
//  UIImage+Portrait.swift
//  VisionCamera
//

import UIKit

extension UIImage {
  private func renderedWithCurrentOrientation() -> CGImage? {
    guard let cgImage else {
      return nil
    }

    if imageOrientation == .up {
      return cgImage
    }

    var transform = CGAffineTransform.identity

    switch imageOrientation {
    case .down, .downMirrored:
      transform = transform.translatedBy(x: size.width, y: size.height).rotated(by: .pi)
    case .left, .leftMirrored:
      transform = transform.translatedBy(x: size.width, y: 0).rotated(by: .pi / 2)
    case .right, .rightMirrored:
      transform = transform.translatedBy(x: 0, y: size.height).rotated(by: -.pi / 2)
    case .up, .upMirrored:
      break
    @unknown default:
      break
    }

    switch imageOrientation {
    case .upMirrored, .downMirrored:
      transform = transform.translatedBy(x: size.width, y: 0).scaledBy(x: -1, y: 1)
    case .leftMirrored, .rightMirrored:
      transform = transform.translatedBy(x: size.height, y: 0).scaledBy(x: -1, y: 1)
    case .up, .down, .left, .right:
      break
    @unknown default:
      break
    }

    guard let colorSpace = cgImage.colorSpace,
          let context = CGContext(data: nil,
                                  width: Int(size.width),
                                  height: Int(size.height),
                                  bitsPerComponent: cgImage.bitsPerComponent,
                                  bytesPerRow: 0,
                                  space: colorSpace,
                                  bitmapInfo: cgImage.bitmapInfo.rawValue) else {
      return nil
    }

    context.concatenate(transform)

    switch imageOrientation {
    case .left, .leftMirrored, .right, .rightMirrored:
      context.draw(cgImage, in: CGRect(x: 0, y: 0, width: size.height, height: size.width))
    default:
      context.draw(cgImage, in: CGRect(x: 0, y: 0, width: size.width, height: size.height))
    }

    return context.makeImage()
  }

  private func rotatingToPortraitIfNeeded(_ cgImage: CGImage) -> CGImage {
    if cgImage.height >= cgImage.width {
      return cgImage
    }

    guard let colorSpace = cgImage.colorSpace,
          let context = CGContext(data: nil,
                                  width: cgImage.height,
                                  height: cgImage.width,
                                  bitsPerComponent: cgImage.bitsPerComponent,
                                  bytesPerRow: 0,
                                  space: colorSpace,
                                  bitmapInfo: cgImage.bitmapInfo.rawValue) else {
      return cgImage
    }

    context.translateBy(x: CGFloat(cgImage.height), y: 0)
    context.rotate(by: .pi / 2)
    context.draw(cgImage, in: CGRect(x: 0, y: 0, width: cgImage.width, height: cgImage.height))

    return context.makeImage() ?? cgImage
  }

  func enforcingPortraitOrientation() -> UIImage {
    guard let rendered = renderedWithCurrentOrientation() else {
      return self
    }
    let portrait = rotatingToPortraitIfNeeded(rendered)
    return UIImage(cgImage: portrait, scale: 1.0, orientation: .up)
  }
}
