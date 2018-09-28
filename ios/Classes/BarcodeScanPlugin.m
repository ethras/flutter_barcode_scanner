#import "BarcodeScanPlugin.h"
#import <barcode_scanner/barcode_scanner-Swift.h>

@implementation BarcodeScanPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterBarcodeReaderPlugin registerWithRegistrar:registrar];
}
@end
