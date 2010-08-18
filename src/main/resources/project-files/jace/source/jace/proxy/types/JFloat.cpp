

#include "jace/proxy/types/JFloat.h"

#ifndef JACE_JCLASS_IMPL_H
#include "jace/JClassImpl.h"
#endif
using jace::JClassImpl;

#include "jace/BoostWarningOff.h"
#include <boost/thread/mutex.hpp>
#include "jace/BoostWarningOff.h"

BEGIN_NAMESPACE_3( jace, proxy, types )


JFloat::JFloat( jvalue value )
{
  setJavaJniValue( value );
}

JFloat::JFloat( jfloat float_ )
{
  jvalue value;
  value.f = float_;
  setJavaJniValue( value );
}

JFloat::~JFloat()
{}

JFloat::operator jfloat() const
{
  return getJavaJniValue().f;
}

jfloat JFloat::getFloat() const
{
  return getJavaJniValue().f;
}

bool JFloat::operator==( const JFloat& float_ ) const
{
  return float_.getFloat() == getFloat();
}

bool JFloat::operator!=( const JFloat& float_ ) const
{
  return !( *this == float_ );
}

bool JFloat::operator==( jfloat val ) const
{
  return val == getFloat();
}

bool JFloat::operator!=( jfloat val ) const
{
  return ! ( *this == val );
}

static boost::mutex javaClassMutex;
const JClass& JFloat::staticGetJavaJniClass() throw ( JNIException )
{
	static boost::shared_ptr<JClassImpl> result;
	boost::mutex::scoped_lock lock(javaClassMutex);
	if (result == 0)
		result = boost::shared_ptr<JClassImpl>(new JClassImpl("float", "F"));
	return *result;
}

const JClass& JFloat::getJavaJniClass() const throw ( JNIException )
{
  return JFloat::staticGetJavaJniClass();
}


END_NAMESPACE_3( jace, proxy, types )

