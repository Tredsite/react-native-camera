//
//  Orientation.m
//

#import "LockOrientation.h"
#import "AppDelegate.h"


@implementation AppDelegate (Orientation)

- (NSUInteger)application:(UIApplication *)application supportedInterfaceOrientationsForWindow:(UIWindow *)window {
  int orientation = [LockOrientation getOrientation];
  switch (orientation) {
    case 1:
      return UIInterfaceOrientationMaskPortrait;
      break;
    case 2:
      return UIInterfaceOrientationMaskLandscape;
      break;
    case 3:
      return UIInterfaceOrientationMaskAllButUpsideDown;
      break;
    default:
      return UIInterfaceOrientationMaskPortrait;
      break;
  }
}

@end


@implementation LockOrientation

@synthesize bridge = _bridge;

static int _orientation = 3;
+ (void)setOrientation: (int)orientation {
  _orientation = orientation;
}

+ (int)getOrientation {
  return _orientation;
}

- (instancetype)init
{
  return self;
}


- (void)dealloc
{
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(lockToPortrait)
{
  [LockOrientation setOrientation:1];
	[[NSOperationQueue mainQueue] addOperationWithBlock:^ {
		[[UIApplication sharedApplication] setStatusBarOrientation:UIInterfaceOrientationPortrait];
		[[UIDevice currentDevice] setValue:[NSNumber numberWithInteger: UIInterfaceOrientationPortrait] forKey:@"orientation"];
		[UIViewController attemptRotationToDeviceOrientation];
  }];

}

RCT_EXPORT_METHOD(lockToLandscapeLeft)
{
  [LockOrientation setOrientation:2];
  [[NSOperationQueue mainQueue] addOperationWithBlock:^ {
	  [[UIApplication sharedApplication] setStatusBarOrientation:UIInterfaceOrientationLandscapeLeft];
	  [[UIDevice currentDevice] setValue:[NSNumber numberWithInteger: UIInterfaceOrientationLandscapeLeft] forKey:@"orientation"];
	  [UIViewController attemptRotationToDeviceOrientation];
  }];
}

RCT_EXPORT_METHOD(lockToLandscapeRight)
{
	[LockOrientation setOrientation:2];
	[[NSOperationQueue mainQueue] addOperationWithBlock:^ {
		[[UIApplication sharedApplication] setStatusBarOrientation:UIInterfaceOrientationLandscapeRight];
		[[UIDevice currentDevice] setValue:[NSNumber numberWithInteger: UIInterfaceOrientationLandscapeRight] forKey:@"orientation"];
		[UIViewController attemptRotationToDeviceOrientation];
	}];
}

RCT_EXPORT_METHOD(unlockAllOrientations)
{
  NSLog(@"Unlock All Orientations");
  [LockOrientation setOrientation:3];
//  AppDelegate *delegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
//  delegate.orientation = 3;
}


@end

