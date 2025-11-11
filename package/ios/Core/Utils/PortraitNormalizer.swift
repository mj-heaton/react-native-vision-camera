//
//  PortraitNormalizer.swift
//  VisionCamera
//

import AVFoundation
import CoreMedia
import Foundation
import ImageIO
import UIKit

enum PortraitNormalizer {
  static func enforcePortrait(at url: URL, metadata: [AnyHashable: Any]) throws -> CGSize {
    let start = CFAbsoluteTimeGetCurrent()
    let data = try Data(contentsOf: url)
    guard var image = UIImage(data: data) else {
      throw CameraError.capture(.imageDataAccessError)
    }

    image = image.enforcingPortraitOrientation()
    try write(image: image, metadata: metadata, to: url)

    let duration = (CFAbsoluteTimeGetCurrent() - start) * 1_000
    let pixelWidth = image.pixelWidth
    let pixelHeight = image.pixelHeight
    VisionLogger.log(level: .info, message: "Portrait normalization took \(duration) ms (\(pixelWidth)x\(pixelHeight)).")

    return CGSize(width: CGFloat(pixelWidth), height: CGFloat(pixelHeight))
  }

  private static func write(image: UIImage, metadata: [AnyHashable: Any], to url: URL) throws {
    guard let cgImage = image.cgImage else {
      throw CameraError.capture(.imageDataAccessError)
    }
    guard let destination = CGImageDestinationCreateWithURL(url as CFURL, AVFileType.jpeg as CFString, 1, nil) else {
      throw CameraError.capture(.imageDataAccessError)
    }

    var sanitizedMetadata = Dictionary(uniqueKeysWithValues: metadata.compactMap { (key, value) -> (String, Any)? in
      guard let stringKey = key as? String else { return nil }
      return (stringKey, value)
    })
    sanitizedMetadata[kCGImagePropertyOrientation as String] = nil

    CGImageDestinationAddImage(destination, cgImage, sanitizedMetadata as CFDictionary)

    if !CGImageDestinationFinalize(destination) {
      throw CameraError.capture(.imageDataAccessError)
    }
  }
}

private extension UIImage {
  var pixelWidth: Int {
    return Int(size.width * scale)
  }

  var pixelHeight: Int {
    return Int(size.height * scale)
  }
}
