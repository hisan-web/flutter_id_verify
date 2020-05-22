#import "IdverifyPlugin.h"
#if __has_include(<idverify/idverify-Swift.h>)
#import <idverify/idverify-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "idverify-Swift.h"
#endif

@implementation IdverifyPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftIdverifyPlugin registerWithRegistrar:registrar];
}
@end
